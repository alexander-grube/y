package cto.shadow.controllers;

import cto.shadow.config.Config;
import cto.shadow.data.Media;
import cto.shadow.data.MediaType;
import cto.shadow.database.Database;
import cto.shadow.middleware.JwtAuthMiddleware;
import io.github.mojtabaJ.cwebp.WebpConverter;
import io.jsonwebtoken.Claims;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import io.undertow.server.HttpServerExchange;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.jboss.logging.Logger;
import org.bytedeco.ffmpeg.global.avcodec;
import com.alibaba.fastjson2.JSON;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileUploadController {
    private static final Logger LOGGER = Logger.getLogger(FileUploadController.class);

    public static void uploadImage(HttpServerExchange exchange) {
        // Get user ID from JWT claims
        Claims claims = exchange.getAttachment(JwtAuthMiddleware.CLAIMS_KEY);
        if (claims == null) {
            exchange.setStatusCode(401);
            exchange.getResponseSender().send("Unauthorized");
            return;
        }

        long userId = Long.parseLong(claims.getSubject());

        exchange.getRequestReceiver().receiveFullBytes((request, bytes) -> {
            final String url;
            final String imageName;
            try {
                final byte[] imageBytes = WebpConverter.imageByteToWebpByte(bytes);
                imageName = "image_" + UUID.randomUUID() + "_" + System.currentTimeMillis() + ".webp";
                final ObjectWriteResponse writeResponse = Database.minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(Config.MINIO_BUCKET_IMAGES)
                                .object(imageName)
                                .stream(new ByteArrayInputStream(imageBytes), imageBytes.length, -1)
                                .contentType("image/webp")
                                .build()
                );
                url = Database.minioClient.getPresignedObjectUrl(
                        GetPresignedObjectUrlArgs.builder()
                                .object(writeResponse.object())
                                .bucket(Config.MINIO_BUCKET_IMAGES)
                                .method(Method.GET)
                                .build()
                );

                // Save image info to database
                try (Connection conn = Database.dataSource.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(
                             "INSERT INTO USER_IMAGES (user_id, image_url, image_name) VALUES (?, ?, ?)")) {
                    stmt.setLong(1, userId);
                    stmt.setString(2, url);
                    stmt.setString(3, imageName);
                    stmt.executeUpdate();
                }

            } catch (Exception e) {
                LOGGER.error("Error uploading image", e);
                exchange.setStatusCode(500);
                exchange.getResponseSender().send("Internal server error");
                return;
            }
            exchange.setStatusCode(200);
            exchange.getResponseSender().send(url);
        });
    }

    public static void uploadVideo(HttpServerExchange exchange) {
        // Get user ID from JWT claims
        Claims claims = exchange.getAttachment(JwtAuthMiddleware.CLAIMS_KEY);
        if (claims == null) {
            exchange.setStatusCode(401);
            exchange.getResponseSender().send("Unauthorized");
            return;
        }

        long userId = Long.parseLong(claims.getSubject());

        exchange.getRequestReceiver().receiveFullBytes((request, bytes) -> {
            final String url;
            final String videoName;
            ByteArrayInputStream byteArrayInputStream;
            ByteArrayOutputStream byteArrayOutputStream;
            FFmpegFrameGrabber grabber = null;
            FFmpegFrameRecorder recorder = null;

            videoName = "video_" + UUID.randomUUID() + "_" + System.currentTimeMillis() + ".webm";

            try {
                byteArrayInputStream = new ByteArrayInputStream(bytes);
                byteArrayOutputStream = new ByteArrayOutputStream();

                grabber = new FFmpegFrameGrabber(byteArrayInputStream);
                grabber.start();

                byte[] videoBytes = bytes;
                if (grabber.getFormat() != null && !grabber.getFormat().contains("webm")) {
                    recorder = new FFmpegFrameRecorder(
                            byteArrayOutputStream,
                            grabber.getImageWidth(),
                            grabber.getImageHeight(),
                            grabber.getAudioChannels());
                    recorder.setFormat("webm");
                    recorder.setVideoCodec(avcodec.AV_CODEC_ID_VP9);
                    recorder.setAudioCodec(avcodec.AV_CODEC_ID_OPUS);
                    recorder.setAudioChannels(grabber.getAudioChannels());
                    recorder.setSampleRate(48000);

                    recorder.start();

                    Frame frame;
                    while ((frame = grabber.grab()) != null) {
                        recorder.record(frame);
                    }

                    // Explicitly flush to ensure all frames are written
                    recorder.flush();
                    recorder.stop();
                    recorder.close();
                    grabber.stop();
                    grabber.close();
                    videoBytes = byteArrayOutputStream.toByteArray();

                }

                final ObjectWriteResponse writeResponse = Database.minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(Config.MINIO_BUCKET_VIDEOS)
                                .object(videoName)
                                .stream(new ByteArrayInputStream(videoBytes), videoBytes.length, -1)
                                .contentType("video/webm")
                                .build()
                );

                url = Database.minioClient.getPresignedObjectUrl(
                        GetPresignedObjectUrlArgs.builder()
                                .object(writeResponse.object())
                                .bucket(Config.MINIO_BUCKET_VIDEOS)
                                .method(Method.GET)
                                .build()
                );

                // Save video info to database
                try (Connection conn = Database.dataSource.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(
                             "INSERT INTO USER_VIDEOS (user_id, video_url, video_name) VALUES (?, ?, ?)")) {
                    stmt.setLong(1, userId);
                    stmt.setString(2, url);
                    stmt.setString(3, videoName);
                    stmt.executeUpdate();
                }

            } catch (Exception e) {
                LOGGER.error("Error uploading video", e);
                exchange.setStatusCode(500);
                exchange.getResponseSender().send("Internal server error");
                return;
            } finally {
                // Ensure resources are closed
                try {
                    if (recorder != null) recorder.close();
                    if (grabber != null) grabber.close();
                } catch (Exception e) {
                    LOGGER.error("Error closing resources", e);
                }
            }
            exchange.setStatusCode(200);
            exchange.getResponseSender().send(url);
        });
    }

    public static void getUserMedia(HttpServerExchange exchange) {
        // Get user ID from JWT claims
        Claims claims = exchange.getAttachment(JwtAuthMiddleware.CLAIMS_KEY);
        if (claims == null) {
            exchange.setStatusCode(401);
            exchange.getResponseSender().send("Unauthorized");
            return;
        }

        long userId = Long.parseLong(claims.getSubject());
        List<Media> mediaList = new ArrayList<>();

        try (Connection conn = Database.dataSource.getConnection()) {
            // Get user's images
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT image_name, image_url, uploaded_at FROM USER_IMAGES WHERE user_id = ? ORDER BY uploaded_at DESC")) {
                stmt.setLong(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String name = rs.getString("image_name");
                        String url = rs.getString("image_url");
                        Timestamp timestamp = rs.getTimestamp("uploaded_at");
                        OffsetDateTime uploadedAt = timestamp.toInstant().atOffset(OffsetDateTime.now().getOffset());

                        mediaList.add(new Media(name, url, MediaType.IMAGE, uploadedAt));
                    }
                }
            }

            // Get user's videos
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT video_name, video_url, uploaded_at FROM USER_VIDEOS WHERE user_id = ? ORDER BY uploaded_at DESC")) {
                stmt.setLong(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String name = rs.getString("video_name");
                        String url = rs.getString("video_url");
                        Timestamp timestamp = rs.getTimestamp("uploaded_at");
                        OffsetDateTime uploadedAt = timestamp.toInstant().atOffset(OffsetDateTime.now().getOffset());

                        mediaList.add(new Media(name, url, MediaType.VIDEO, uploadedAt));
                    }
                }
            }

            // Sort by upload date (most recent first)
            mediaList.sort((a, b) -> b.uploadedAt().compareTo(a.uploadedAt()));

            String jsonResponse = JSON.toJSONString(mediaList);
            exchange.setStatusCode(200);
            exchange.getResponseHeaders().put(io.undertow.util.Headers.CONTENT_TYPE, "application/json");
            exchange.getResponseSender().send(jsonResponse);

        } catch (Exception e) {
            LOGGER.error("Error retrieving user media", e);
            exchange.setStatusCode(500);
            exchange.getResponseSender().send("Internal server error");
        }
    }
}
