package cto.shadow.controllers;

import cto.shadow.config.Config;
import cto.shadow.database.Database;
import io.minio.PutObjectArgs;
import io.undertow.server.HttpServerExchange;
import org.jboss.logging.Logger;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.UUID;

public class FileUploadController {
    private static final Logger LOGGER = Logger.getLogger(FileUploadController.class);

    private static final ImageWriter WEBP_WRITER = ImageIO.getImageWritersByMIMEType("image/webp").next();

    public static void uploadImage(HttpServerExchange exchange) {
        exchange.getRequestReceiver().receiveFullBytes((request, bytes) -> {
            try {
                final BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(bytes));
                if (bufferedImage == null) {
                    LOGGER.error("Failed to read image");
                    exchange.setStatusCode(400);
                    exchange.getResponseSender().send("Invalid image");
                    return;
                }

                final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                final byte[] imageBytes;
                try (final ImageOutputStream outputStream = ImageIO.createImageOutputStream(byteArrayOutputStream)) {
                    WEBP_WRITER.setOutput(outputStream);
                    WEBP_WRITER.write(null, new IIOImage(bufferedImage, null, null), null);
                    imageBytes = byteArrayOutputStream.toByteArray();
                }
                WEBP_WRITER.dispose();
                final String imageName = "image_" + UUID.randomUUID() + "_" + System.currentTimeMillis() + ".webp"; // Generate a unique name for the image
                Database.minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(Config.MINIO_BUCKET_IMAGES)
                                .object(imageName)
                                .stream(new ByteArrayInputStream(imageBytes), imageBytes.length, -1)
                                .contentType("image/webp")
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
                // TODO: convert everything to mp4
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
