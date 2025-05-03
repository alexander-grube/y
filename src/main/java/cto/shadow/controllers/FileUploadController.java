package cto.shadow.controllers;

import cto.shadow.config.Config;
import cto.shadow.database.Database;
import io.minio.PutObjectArgs;
import io.undertow.server.HttpServerExchange;
import org.jboss.logging.Logger;

import java.io.ByteArrayInputStream;
import java.util.UUID;

public class FileUploadController {
    private static final Logger LOGGER = Logger.getLogger(FileUploadController.class);

    public static void uploadImage(HttpServerExchange exchange) {
        exchange.getRequestReceiver().receiveFullBytes((request, bytes) -> {
            try {
                // TODO: convert everything to webp
                String imageName = "image_" + UUID.randomUUID().toString() + "_" + System.currentTimeMillis() + ".jpg"; // Generate a unique name for the image
                Database.minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(Config.MINIO_BUCKET_IMAGES)
                                .object(imageName)
                                .stream(new ByteArrayInputStream(bytes), bytes.length, -1)
                                .contentType("image/jpeg")
                                .build()
                );
            } catch (Exception e) {
                LOGGER.error("Error uploading image", e);
                exchange.setStatusCode(500);
                exchange.getResponseSender().send("Internal server error");
                return;
            }
            exchange.setStatusCode(200);
            exchange.getResponseSender().send("Image uploaded successfully");
        });
    }

    public static void uploadVideo(HttpServerExchange exchange) {
        exchange.getRequestReceiver().receiveFullBytes((request, bytes) -> {
            try {
                // TODO: convert everything to webm
                String videoName = "video_" + UUID.randomUUID().toString() + "_" + System.currentTimeMillis() + ".mp4"; // Generate a unique name for the video
                Database.minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(Config.MINIO_BUCKET_VIDEOS)
                                .object(videoName)
                                .stream(new ByteArrayInputStream(bytes), bytes.length, -1)
                                .contentType("video/mp4")
                                .build()
                );
            } catch (Exception e) {
                LOGGER.error("Error uploading video", e);
                exchange.setStatusCode(500);
                exchange.getResponseSender().send("Internal server error");
                return;
            }
            exchange.setStatusCode(200);
            exchange.getResponseSender().send("Video uploaded successfully");
        });
    }
}
