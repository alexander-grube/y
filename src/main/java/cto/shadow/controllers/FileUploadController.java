package cto.shadow.controllers;

import cto.shadow.config.Config;
import cto.shadow.database.Database;
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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Objects;
import java.util.UUID;

public class FileUploadController {
    private static final Logger LOGGER = Logger.getLogger(FileUploadController.class);

    public static void uploadImage(HttpServerExchange exchange) {
        exchange.getRequestReceiver().receiveFullBytes((request, bytes) -> {
            final String url;
            try {
                final BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(bytes));
                if (bufferedImage == null) {
                    LOGGER.error("Failed to read image");
                    exchange.setStatusCode(400);
                    exchange.getResponseSender().send("Invalid image");
                    return;
                }

                final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "webp", byteArrayOutputStream);
                byte[] imageBytes = byteArrayOutputStream.toByteArray();
                final String imageName = "image_" + UUID.randomUUID() + "_" + System.currentTimeMillis() + ".webp"; // Generate a unique name for the image
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
        exchange.getRequestReceiver().receiveFullBytes((request, bytes) -> {
            final String url;
            ByteArrayInputStream byteArrayInputStream;
            ByteArrayOutputStream byteArrayOutputStream;
            FFmpegFrameGrabber grabber = null;
            FFmpegFrameRecorder recorder = null;

            String videoName = "video_" + UUID.randomUUID() + "_" + System.currentTimeMillis() + ".webm";

            try {
                byteArrayInputStream = new ByteArrayInputStream(bytes);
                byteArrayOutputStream = new ByteArrayOutputStream();

                grabber = new FFmpegFrameGrabber(byteArrayInputStream);
                grabber.start();

                byte[] videoBytes = bytes;
                if (!Objects.equals(grabber.getFormat(), "webm")) {

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
}
