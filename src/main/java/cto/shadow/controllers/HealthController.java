package cto.shadow.controllers;

import cto.shadow.database.Database;
import io.undertow.server.HttpServerExchange;
import org.jboss.logging.Logger;

import java.sql.Connection;
import java.sql.SQLException;

public class HealthController {
    private static final Logger LOGGER = Logger.getLogger(HealthController.class);

    public static void checkHealth(HttpServerExchange exchange) throws SQLException {
        final Connection connection = Database.dataSource.getConnection();
        if (connection == null) {
            LOGGER.error("Database connection is null");
            exchange.setStatusCode(500);
            exchange.getResponseSender().send("Database connection error");
            return;
        }
        exchange.setStatusCode(200);
        exchange.getResponseSender().send("OK");
    }
}
