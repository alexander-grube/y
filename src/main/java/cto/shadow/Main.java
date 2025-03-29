package cto.shadow;

import cto.shadow.server.Server;
import org.jboss.logging.Logger;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class);

    public static void main(String[] args) {
        LOGGER.info("Get Ready to be Shadowed!");

        Server.bootstrapServer().start();
    }
}