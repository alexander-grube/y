package cto.shadow.controllers;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.alibaba.fastjson2.JSON;
import cto.shadow.config.Config;
import cto.shadow.database.Database;
import cto.shadow.dtos.UpdatePhoneNumberRequest;
import cto.shadow.dtos.UserLogin;
import cto.shadow.dtos.UserRegister;
import cto.shadow.utils.JwtTokenGenerator;
import io.undertow.server.HttpServerExchange;
import org.jboss.logging.Logger;

import java.sql.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class UserController {
    private static final Logger LOGGER = Logger.getLogger(UserController.class);

    public static void registerUser(HttpServerExchange exchange) {
        exchange.getRequestReceiver().receiveFullBytes(((request, bytes) -> {
            Connection connection = null;
            try {
                connection = Database.dataSource.getConnection();
                connection.setAutoCommit(false); // Start transaction

                UserRegister userRegister = JSON.parseObject(bytes, UserRegister.class);
                String hashedPassword = BCrypt.withDefaults().hashToString(Config.BCRYPT_COST, userRegister.password().toCharArray());
                long id;
                try (PreparedStatement statement = connection.prepareStatement(
                        """
                                INSERT INTO users (date_of_birth, first_name, last_name, email, phone, username, password, created_by, modified_by
                                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                                RETURNING id
                                """)) {
                    statement.setObject(1, userRegister.dateOfBirth());
                    statement.setString(2, userRegister.firstName());
                    statement.setString(3, userRegister.lastName());
                    statement.setString(4, userRegister.email());
                    statement.setString(5, userRegister.phone());
                    statement.setString(6, userRegister.username());
                    statement.setString(7, hashedPassword);
                    statement.setString(8, userRegister.email());
                    statement.setString(9, userRegister.email());
                    var resultSet = statement.executeQuery();
                    if (!resultSet.next()) {
                        LOGGER.error("Failed to register user");
                        exchange.setStatusCode(500);
                        exchange.getResponseSender().send("Failed to register user");
                        connection.rollback();
                        return;
                    }
                    id = resultSet.getLong(1);
                }
                long userRoleId;
                try (PreparedStatement statement = connection.prepareStatement(
                        """
                                SELECT id FROM roles WHERE authority = 'USER'
                                """)) {
                    var resultSet = statement.executeQuery();
                    if (!resultSet.next()) {
                        LOGGER.error("Failed to get user role");
                        exchange.setStatusCode(500);
                        exchange.getResponseSender().send("Failed to get user role");
                        connection.rollback();
                        return;
                    }
                    userRoleId = resultSet.getLong(1);
                }
                try (PreparedStatement statement = connection.prepareStatement(
                        """
                                INSERT INTO user_roles (user_id, role_id) VALUES (?, ?)
                                """)) {
                    statement.setLong(1, id);
                    statement.setLong(2, userRoleId);
                    statement.executeUpdate();
                }

                connection.commit(); // Commit transaction if all operations succeed

                exchange.setStatusCode(200);
                exchange.getResponseSender().send("User registered successfully");
            } catch (Exception e) {
                LOGGER.error("Error registering user", e);
                if (connection != null) {
                    try {
                        connection.rollback(); // Rollback transaction on error
                    } catch (SQLException rollbackEx) {
                        LOGGER.error("Error rolling back transaction", rollbackEx);
                    }
                }
                exchange.setStatusCode(500);
                exchange.getResponseSender().send("Internal server error");
            } finally {
                if (connection != null) {
                    try {
                        connection.setAutoCommit(true); // Reset auto-commit mode
                        connection.close();
                    } catch (SQLException closeEx) {
                        LOGGER.error("Error closing connection", closeEx);
                    }
                }
            }
        }));
    }

    public static void loginUser(HttpServerExchange exchange) {
        exchange.getRequestReceiver().receiveFullBytes(((request, bytes) -> {
            Connection connection = null;
            try {
                connection = Database.dataSource.getConnection();
                connection.setAutoCommit(false); // Start transaction

                UserLogin userLogin = JSON.parseObject(bytes, UserLogin.class);
                final String username = userLogin.username();
                final String password = userLogin.password();
                long id;
                String hashedPassword;
                try (PreparedStatement statement = connection.prepareStatement(
                        """
                                SELECT id, password FROM users WHERE username = ?
                                """)) {
                    statement.setString(1, username);
                    var resultSet = statement.executeQuery();
                    if (!resultSet.next()) {
                        LOGGER.error("Invalid username or password");
                        exchange.setStatusCode(401);
                        exchange.getResponseSender().send("Invalid username or password");
                        connection.rollback(); // Rollback transaction
                        return;
                    }
                    id = resultSet.getLong(1);
                    hashedPassword = resultSet.getString(2);
                }
                if (!BCrypt.verifyer().verify(password.toCharArray(), hashedPassword).verified) {
                    LOGGER.error("Invalid username or password");
                    try (PreparedStatement statement = connection.prepareStatement(
                            """
                                    UPDATE users SET failed_login_attempts = failed_login_attempts + 1 WHERE id = ?
                                    """)) {
                        statement.setLong(1, id);
                        statement.executeUpdate();
                    }
                    connection.commit(); // Commit the failed login attempt update
                    exchange.setStatusCode(401);
                    exchange.getResponseSender().send("Invalid username or password");
                    return;
                }
                final String jwt = JwtTokenGenerator.generateToken(id);
                try (PreparedStatement statement = connection.prepareStatement(
                        """
                                UPDATE users SET failed_login_attempts = 0 WHERE id = ?
                                """)) {
                    statement.setLong(1, id);
                    statement.executeUpdate();
                }
                try (PreparedStatement statement = connection.prepareStatement(
                        """
                                UPDATE users SET last_login_at = ? WHERE id = ?
                                """)) {
                    statement.setObject(1, OffsetDateTime.now(ZoneOffset.UTC));
                    statement.setLong(2, id);
                    statement.executeUpdate();
                }

                connection.commit(); // Commit transaction if all operations succeed

                exchange.setStatusCode(200);
                exchange.getResponseSender().send(jwt);
            } catch (Exception e) {
                LOGGER.error("Error logging in user", e);
                if (connection != null) {
                    try {
                        connection.rollback(); // Rollback transaction on error
                    } catch (SQLException rollbackEx) {
                        LOGGER.error("Error rolling back transaction", rollbackEx);
                    }
                }
                exchange.setStatusCode(500);
                exchange.getResponseSender().send("Internal server error");
            } finally {
                if (connection != null) {
                    try {
                        connection.setAutoCommit(true); // Reset auto-commit mode
                        connection.close();
                    } catch (SQLException closeEx) {
                        LOGGER.error("Error closing connection", closeEx);
                    }
                }
            }
        }));
    }

    public static void updatePhoneNumber(HttpServerExchange exchange) {
        final long id = Long.parseLong(exchange.getQueryParameters().get("id").getFirst());
        exchange.getRequestReceiver().receiveFullBytes((request, bytes) -> {
            Connection connection = null;
            try {
                connection = Database.dataSource.getConnection();
                connection.setAutoCommit(false); // Start transaction

                UpdatePhoneNumberRequest updatePhoneNumberRequest;
                try {
                    updatePhoneNumberRequest = JSON.parseObject(bytes, UpdatePhoneNumberRequest.class);
                } catch (Exception e) {
                    LOGGER.error("Error parsing request body", e);
                    exchange.setStatusCode(400);
                    exchange.getResponseSender().send("Invalid request body " + e.getMessage());
                    connection.rollback(); // Rollback transaction
                    return;
                }

                try (PreparedStatement statement = connection.prepareStatement(
                        """
                                UPDATE users SET phone = ?, modified_at = ?, modified_by = ? WHERE id = ?
                                """)) {
                    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
                    statement.setString(1, updatePhoneNumberRequest.phone());
                    statement.setObject(2, now);
                    statement.setString(3, "system");
                    statement.setLong(4, id);
                    int rowsUpdated = statement.executeUpdate();
                    if (rowsUpdated == 0) {
                        exchange.setStatusCode(404);
                        exchange.getResponseSender().send("User not found");
                        connection.rollback(); // Rollback transaction
                        return;
                    }
                }

                connection.commit(); // Commit transaction if all operations succeed

                exchange.setStatusCode(200);
                exchange.getResponseSender().send("Phone number updated successfully");
            } catch (Exception e) {
                LOGGER.error("Error updating phone number", e);
                if (connection != null) {
                    try {
                        connection.rollback(); // Rollback transaction on error
                    } catch (SQLException rollbackEx) {
                        LOGGER.error("Error rolling back transaction", rollbackEx);
                    }
                }
                exchange.setStatusCode(500);
                exchange.getResponseSender().send("Internal server error");
            } finally {
                if (connection != null) {
                    try {
                        connection.setAutoCommit(true); // Reset auto-commit mode
                        connection.close();
                    } catch (SQLException closeEx) {
                        LOGGER.error("Error closing connection", closeEx);
                    }
                }
            }
        });
    }
}
