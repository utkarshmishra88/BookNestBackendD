package com.booknest.wallet.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtUtil {

    private final SecretKey key;

    public JwtUtil(@Value("${jwt.secret}") String jwtSecret) {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);

        Object userId = claims.get("userId");
        if (userId instanceof Integer i) return i.longValue();
        if (userId instanceof Long l) return l;
        if (userId instanceof String s && !s.isBlank()) {
            try {
                return Long.parseLong(s);
            } catch (Exception ignored) {
            }
        }

        String sub = claims.getSubject();
        if (sub != null && !sub.isBlank()) {
            try {
                return Long.parseLong(sub);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    public String extractRole(String token) {
        Object role = extractAllClaims(token).get("role");
        return role == null ? null : String.valueOf(role);
    }
}