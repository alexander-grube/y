package cto.shadow.server;

import cto.shadow.config.Config;
import cto.shadow.controllers.*;
import cto.shadow.middleware.JwtAuthMiddleware;
import cto.shadow.routes.Routes;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;

public class Server {

    public static Undertow bootstrapServer() {
        return Undertow.builder().addHttpListener(Config.PORT, Config.HOST, HANDLER)
                .setIoThreads(10)
                .setWorkerThreads(10)
                .build();
    }

    private static final HttpHandler ROUTES = new RoutingHandler()
            .get(Routes.HEALTH_CHECK, HealthController::checkHealth)
            .post(Routes.ECHO, EchoController::echo)
            .post(Routes.ECHO_JWT_CLAIMS, EchoController::echoJwtClaims)
            .post(Routes.USER_REGISTER, UserController::registerUser)
            .post(Routes.USER_LOGIN, UserController::loginUser)
            .put(Routes.USER_UPDATE_PHONE_NUMBER, UserController::updatePhoneNumber)
            .put(Routes.USER_UPDATE_PASSWORD, UserController::updatePassword)
            .post(Routes.FOLLOW_USER, FollowController::followUser)
            .post(Routes.UNFOLLOW_USER, FollowController::unfollowUser)
            .post(Routes.UPLOAD_IMAGE, FileUploadController::uploadImage)
            .post(Routes.UPLOAD_VIDEO, FileUploadController::uploadVideo)
            .get(Routes.USER_MEDIA, FileUploadController::getUserMedia);

    private static final HttpHandler HANDLER = new JwtAuthMiddleware(ROUTES);
}
