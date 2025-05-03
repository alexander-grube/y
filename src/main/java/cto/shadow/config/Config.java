package cto.shadow.config;

import io.github.cdimascio.dotenv.Dotenv;

public class Config {

    private static final Dotenv dotenv = Dotenv.load();

    public static final String DB_URL = dotenv.get("DB_URL");

    public static final String DB_USER = dotenv.get("DB_USER");

    public static final String DB_PASSWORD = dotenv.get("DB_PASSWORD");

    public static final int PORT = Integer.parseInt(dotenv.get("PORT"));

    public static final String HOST = dotenv.get("HOST");

    public static final int BCRYPT_COST = 12;

    public static final String JWT_SECRET = dotenv.get("JWT_SECRET");

    public static final long JWT_EXPIRATION_MILLIS = Long.parseLong(dotenv.get("JWT_EXPIRATION_MILLIS"));

    public static final String JWT_ISSUER = dotenv.get("JWT_ISSUER");

    public static final String MINIO_ENDPOINT = dotenv.get("MINIO_ENDPOINT");

    public static final String MINIO_ACCESS_KEY = dotenv.get("MINIO_ACCESS_KEY");

    public static final String MINIO_SECRET_KEY = dotenv.get("MINIO_SECRET_KEY");

    public static final String MINIO_BUCKET_IMAGES = dotenv.get("MINIO_BUCKET_IMAGES");

    public static final String MINIO_BUCKET_VIDEOS = dotenv.get("MINIO_BUCKET_VIDEOS");
}
