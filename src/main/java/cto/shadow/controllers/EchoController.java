package cto.shadow.controllers;

import cto.shadow.middleware.JwtAuthMiddleware;
import io.jsonwebtoken.Claims;
import io.undertow.server.HttpServerExchange;

public class EchoController {
    public static void echo(HttpServerExchange exchange) {
        exchange.getRequestReceiver().receiveFullString((request, message) -> {
            exchange.setStatusCode(200);
            exchange.getResponseSender().send(message);
        });
    }

    public static void echoJwtClaims(HttpServerExchange exchange) {
        exchange.getRequestReceiver().receiveFullString((request, message) -> {
            Claims claims = exchange.getAttachment(JwtAuthMiddleware.CLAIMS_KEY);
            exchange.setStatusCode(200);
            exchange.getResponseSender().send("JWT Claims: " + claims.toString());
        });
    }
}
