package cto.shadow.server;

import cto.shadow.config.Config;
import cto.shadow.controllers.EchoController;
import cto.shadow.controllers.HealthController;
import cto.shadow.controllers.UserController;
import cto.shadow.routes.Routes;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;

public class Server {

    public static Undertow bootstrapServer() {
        return Undertow.builder().addHttpListener(Config.PORT, Config.HOST, ROUTES)
                .build();
    }

    private static final HttpHandler ROUTES = new RoutingHandler()
            .get(Routes.HEALTH_CHECK, HealthController::checkHealth)
            .post(Routes.ECHO, EchoController::echo)
            .post(Routes.USER_REGISTER, UserController::registerUser)
            .post(Routes.USER_LOGIN, UserController::loginUser)
            .put(Routes.USER_UPDATE_PHONE_NUMBER, UserController::updatePhoneNumber);
}
