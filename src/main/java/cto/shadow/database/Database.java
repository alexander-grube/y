package cto.shadow.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import cto.shadow.config.Config;

public class Database {

    public static final HikariDataSource dataSource;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(Config.DB_URL);
        config.setUsername(Config.DB_USER);
        config.setPassword(Config.DB_PASSWORD);
        config.addDataSourceProperty("maximumPoolSize", "25");

        dataSource = new HikariDataSource(config);
    }
}
