package cto.shadow.controllers;

import io.undertow.server.HttpServerExchange;

public class EchoController {
    public static void echo(HttpServerExchange exchange) {
        exchange.getRequestReceiver().receiveFullString((request, message) -> {
            exchange.setStatusCode(200);
            exchange.getResponseSender().send(message);
        });
    }
}
