package cto.shadow.utils;

import cto.shadow.config.Config;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.AeadAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtUtils {
    public static String generateToken(final long userId) {
        final SecretKey key = Keys.hmacShaKeyFor(Config.JWT_SECRET.getBytes(StandardCharsets.UTF_8));

        final Date now = new Date();
        final Date expirationDate = new Date(now.getTime() + Config.JWT_EXPIRATION_MILLIS);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuer(Config.JWT_ISSUER)
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(key)
                .compact();
    }

    public static Claims parseToken(final String token) {
        final SecretKey key = Keys.hmacShaKeyFor(Config.JWT_SECRET.getBytes(StandardCharsets.UTF_8));

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
