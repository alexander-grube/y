package cto.shadow.controllers;

import cto.shadow.config.Config;
import cto.shadow.database.Database;
import io.minio.BucketExistsArgs;
import io.undertow.server.HttpServerExchange;
import org.jboss.logging.Logger;

import java.sql.Connection;
import java.sql.SQLException;

public class HealthController {
    private static final Logger LOGGER = Logger.getLogger(HealthController.class);

    public static void checkHealth(HttpServerExchange exchange) throws Exception {
        final Connection connection = Database.dataSource.getConnection();
        if (connection == null) {
            LOGGER.error("Database connection is null");
            exchange.setStatusCode(500);
            exchange.getResponseSender().send("Database connection error");
            return;
        }
        final boolean foundImagesBucket = Database.minioClient.bucketExists(BucketExistsArgs.builder().bucket(Config.MINIO_BUCKET_IMAGES).build());
        if (!foundImagesBucket) {
            LOGGER.error("Images bucket does not exist");
            exchange.setStatusCode(500);
            exchange.getResponseSender().send("Images bucket does not exist");
            return;
        }
        final boolean foundVideosBucket = Database.minioClient.bucketExists(BucketExistsArgs.builder().bucket(Config.MINIO_BUCKET_VIDEOS).build());
        if (!foundVideosBucket) {
            LOGGER.error("Videos bucket does not exist");
            exchange.setStatusCode(500);
            exchange.getResponseSender().send("Videos bucket does not exist");
            return;
        }
        exchange.setStatusCode(200);
        exchange.getResponseSender().send("OK");
    }
}
