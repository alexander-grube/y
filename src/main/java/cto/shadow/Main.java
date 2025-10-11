package cto.shadow;

import cto.shadow.server.Server;
import org.jboss.logging.Logger;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class);

    static void main() {
        LOGGER.info("Get Ready to be Shadowed!");
        Server.bootstrapServer().start();
    }
}