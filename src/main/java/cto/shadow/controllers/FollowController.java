package cto.shadow.controllers;

import cto.shadow.database.Database;
import cto.shadow.middleware.JwtAuthMiddleware;
import io.undertow.server.HttpServerExchange;
import org.jboss.logging.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class FollowController {
    private static final Logger LOGGER = Logger.getLogger(FollowController.class);

    public static void followUser(HttpServerExchange exchange) {
        final long id = Long.parseLong(exchange.getAttachment(JwtAuthMiddleware.CLAIMS_KEY).get("sub", String.class));
        exchange.getRequestReceiver().receiveFullBytes((request, bytes) -> {
            Connection connection = null;
            try {
                connection = Database.dataSource.getConnection();
                connection.setAutoCommit(false); // Start transaction

                final long followUserId = Long.parseLong(exchange.getQueryParameters().get("id").getFirst());

                try (PreparedStatement statement = connection.prepareStatement(
                        """
                                INSERT INTO follows (follower_id, followed_id) VALUES (?, ?)
                                """)) {
                    statement.setLong(1, id);
                    statement.setLong(2, followUserId);
                    statement.executeUpdate();
                }

                connection.commit(); // Commit transaction if all operations succeed

                exchange.setStatusCode(200);
                exchange.getResponseSender().send("Followed user successfully");
            } catch (Exception e) {
                LOGGER.error("Error following user", e);
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

    public static void unfollowUser(HttpServerExchange exchange) {
        final long id = Long.parseLong(exchange.getAttachment(JwtAuthMiddleware.CLAIMS_KEY).get("sub", String.class));
        exchange.getRequestReceiver().receiveFullBytes((request, bytes) -> {
            Connection connection = null;
            try {
                connection = Database.dataSource.getConnection();
                connection.setAutoCommit(false); // Start transaction

                final long unfollowUserId = Long.parseLong(exchange.getQueryParameters().get("id").getFirst());

                try (PreparedStatement statement = connection.prepareStatement(
                        """
                                DELETE FROM follows WHERE follower_id = ? AND followed_id = ?
                                """)) {
                    statement.setLong(1, id);
                    statement.setLong(2, unfollowUserId);
                    statement.executeUpdate();
                }

                connection.commit(); // Commit transaction if all operations succeed

                exchange.setStatusCode(200);
                exchange.getResponseSender().send("Unfollowed user successfully");
            } catch (Exception e) {
                LOGGER.error("Error unfollowing user", e);
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
