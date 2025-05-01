package cto.shadow.middleware;

import cto.shadow.routes.Routes;
import cto.shadow.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.AttachmentKey;

public class JwtAuthMiddleware implements HttpHandler {
    private final HttpHandler next;
    private static final String AUTH_HEADER = "Authorization";
    private static final String AUTH_METHOD_PREFIX = "Bearer ";
    public static final AttachmentKey<Claims> CLAIMS_KEY = AttachmentKey.create(Claims.class);

    public JwtAuthMiddleware(HttpHandler next) {
        this.next = next;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (exchange.getRequestPath().startsWith(Routes.HEALTH_CHECK)) {
            next.handleRequest(exchange);
            return;
        }

        String authHeader = exchange.getRequestHeaders().getFirst(AUTH_HEADER);
        if (authHeader == null || !authHeader.startsWith(AUTH_METHOD_PREFIX)) {
            exchange.setStatusCode(401);
            exchange.getResponseSender().send("Wrong authorization header");
            return;
        }

        try {
            String token = authHeader.substring(AUTH_METHOD_PREFIX.length());

            Claims claims = JwtUtils.parseToken(token);
            if (claims == null) {
                exchange.setStatusCode(401);
                exchange.getResponseSender().send("Token parse error");
                return;
            }

            exchange.putAttachment(CLAIMS_KEY, claims);
            next.handleRequest(exchange);
        } catch (Exception e) {
            exchange.setStatusCode(401);
            exchange.getResponseSender().send("Unauthorized");
        } finally {
            exchange.removeAttachment(CLAIMS_KEY);
        }
    }
}
