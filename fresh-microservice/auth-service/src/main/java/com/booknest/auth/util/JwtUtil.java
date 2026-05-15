package com.booknest.auth.util;

import com.booknest.auth.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.UUID;

/**
 * JWT helper utilities.
 *
 * - Use `jwt.secret` property (must be long enough for HS256).
 * - Provides generation from User or generic claims map.
 * - Provides safe claim extraction and token validation with logging.
 */
@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret:YourSuperSecretKeyForBookNestPlatformEnsureItIsLongEnough}")
    private String secret;

    @Value("${jwt.expiration-ms:86400000}") // default 24h
    private long expirationTime;

    private Key getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate a token from a User entity with standard claims.
     */
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        if (user != null) {
            claims.put("userId", user.getUserId());
            claims.put("email", user.getEmail() != null ? user.getEmail() : "");
            claims.put("name", user.getFullName() != null ? user.getFullName() : "");
            claims.put("role", user.getRole() != null ? user.getRole() : "CUSTOMER");
        }
        return generateToken(claims);
    }

    /**
     * Generate a token from an arbitrary claims map.
     */
    public String generateToken(Map<String, Object> claims) {
        return generateToken(claims, expirationTime);
    }

    public String generateToken(Map<String, Object> claims, long customExpirationMs) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + customExpirationMs);

        String subject = null;
        if (claims != null) {
            if (claims.get("userId") != null) {
                subject = String.valueOf(claims.get("userId"));
            } else if (claims.get("email") != null && !String.valueOf(claims.get("email")).isBlank()) {
                subject = String.valueOf(claims.get("email"));
            }
        }
        if (subject == null || subject.isBlank()) {
            subject = "unknown";
        }

        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setClaims(claims != null ? new HashMap<>(claims) : new HashMap<>())
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extract all claims from token (throws runtime exceptions for invalid token).
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Generic claim extractor.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        try {
            final Claims claims = extractAllClaims(token);
            return claimsResolver.apply(claims);
        } catch (ExpiredJwtException eje) {
            log.warn("Token expired while extracting claim: {}", eje.getMessage());
            throw eje;
        } catch (SecurityException | MalformedJwtException mre) {
            log.warn("Invalid JWT signature/format: {}", mre.getMessage());
            throw mre;
        } catch (Exception e) {
            log.error("Failed to extract claim from token: {}", e.getMessage());
            throw e;
        }
    }

    public Integer extractUserId(String token) {
        try {
            Object userIdClaim = extractClaim(token, c -> c.get("userId"));
            if (userIdClaim != null) {
                return Integer.valueOf(String.valueOf(userIdClaim));
            }
            String sub = extractClaim(token, Claims::getSubject);
            return sub != null ? Integer.valueOf(sub) : null;
        } catch (Exception e) {
            log.warn("Could not extract userId: {}", e.getMessage());
            return null;
        }
    }

    public String extractEmail(String token) {
        try {
            Object email = extractClaim(token, c -> c.get("email"));
            if (email != null) return String.valueOf(email);
            return extractClaim(token, Claims::getSubject);
        } catch (Exception e) {
            log.warn("Could not extract email: {}", e.getMessage());
            return null;
        }
    }

    public String extractRole(String token) {
        try {
            return extractClaim(token, c -> c.get("role", String.class));
        } catch (Exception e) {
            log.warn("Could not extract role: {}", e.getMessage());
            return null;
        }
    }

    private boolean isTokenExpired(String token) {
        try {
            Date expiration = extractClaim(token, Claims::getExpiration);
            return expiration.before(new Date());
        } catch (ExpiredJwtException eje) {
            return true;
        } catch (Exception e) {
            log.warn("Error checking token expiry: {}", e.getMessage());
            return true;
        }
    }

    /**
     * Validate token signature and expiration.
     */
    public boolean validateToken(String token) {
        try {
            // parse to validate signature; throws on invalid
            extractAllClaims(token);
            // also check expiry explicitly
            if (isTokenExpired(token)) {
                log.warn("JWT validation failed: token expired");
                return false;
            }
            return true;
        } catch (ExpiredJwtException eje) {
            log.warn("JWT expired: {}", eje.getMessage());
        } catch (SecurityException | MalformedJwtException mre) {
            log.warn("JWT invalid: {}", mre.getMessage());
        } catch (Exception e) {
            log.error("JWT validation error: {}", e.getMessage());
        }
        return false;
    }
}