package cto.shadow.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import cto.shadow.config.Config;
import io.minio.MinioClient;

public class Database {

    public static final HikariDataSource dataSource;
    public static final MinioClient minioClient;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(Config.DB_URL);
        config.setUsername(Config.DB_USER);
        config.setPassword(Config.DB_PASSWORD);
        config.addDataSourceProperty("maximumPoolSize", "25");

        dataSource = new HikariDataSource(config);

        minioClient = MinioClient.builder()
                .endpoint(Config.MINIO_ENDPOINT)
                .credentials(Config.MINIO_ACCESS_KEY, Config.MINIO_SECRET_KEY)
                .build();
    }
}
