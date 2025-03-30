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
}
