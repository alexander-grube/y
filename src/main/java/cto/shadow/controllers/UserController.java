package cto.shadow.controllers;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.alibaba.fastjson2.JSON;
import cto.shadow.config.Config;
import cto.shadow.database.Database;
import cto.shadow.dtos.UpdatePhoneNumberRequest;
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
            try (Connection connection = Database.dataSource.getConnection()) {
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
                final String jwt = JwtTokenGenerator.generateToken(id);
                exchange.setStatusCode(200);
                exchange.getResponseSender().send(jwt);
            } catch (Exception e) {
                LOGGER.error("Error registering user", e);
                exchange.setStatusCode(500);
                exchange.getResponseSender().send("Internal server error");
            }
        }));
    }

    public static void updatePhoneNumber(HttpServerExchange exchange) {
        final long id = Long.parseLong(exchange.getQueryParameters().get("id").getFirst());
        exchange.getRequestReceiver().receiveFullBytes((request, bytes) -> {
            try (Connection connection = Database.dataSource.getConnection()) {
                UpdatePhoneNumberRequest updatePhoneNumberRequest;
                try {
                    updatePhoneNumberRequest = JSON.parseObject(bytes, UpdatePhoneNumberRequest.class);
                } catch (Exception e) {
                    LOGGER.error("Error parsing request body", e);
                    exchange.setStatusCode(400);
                    exchange.getResponseSender().send("Invalid request body " + e.getMessage());
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
                        return;
                    }
                }
                exchange.setStatusCode(200);
                exchange.getResponseSender().send("Phone number updated successfully");
            } catch (Exception e) {
                LOGGER.error("Error updating phone number", e);
                exchange.setStatusCode(500);
                exchange.getResponseSender().send("Internal server error");
            }
        });
    }
}
