package cto.shadow.controllers;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.alibaba.fastjson2.JSON;
import cto.shadow.config.Config;
import cto.shadow.database.Database;
import cto.shadow.dtos.UserRegister;
import io.undertow.server.HttpServerExchange;
import org.jboss.logging.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class UserController {
    private static final Logger LOGGER = Logger.getLogger(UserController.class);

    public static void registerUser(HttpServerExchange exchange) {
        exchange.getRequestReceiver().receiveFullBytes(((request, bytes) -> {
            try (Connection connection = Database.dataSource.getConnection()) {
                UserRegister userRegister = JSON.parseObject(bytes, UserRegister.class);
                String hashedPassword = BCrypt.withDefaults().hashToString(Config.BCRYPT_COST, userRegister.password().toCharArray());
                try (PreparedStatement statement = connection.prepareStatement(
                        """
                                INSERT INTO users (date_of_birth, first_name, last_name, email, phone, username, password, created_by, modified_by
                                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                                RETURNING id
                                """)) {
                    statement.setDate(1, userRegister.dateOfBirth());
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
                    exchange.setStatusCode(200);
                    exchange.getResponseSender().send("User with ID " + resultSet.getInt(1) + " registered successfully");
                }
            } catch (Exception e) {
                LOGGER.error("Error registering user", e);
                exchange.setStatusCode(500);
                exchange.getResponseSender().send("Internal server error");
            }
        }));
    }
}
