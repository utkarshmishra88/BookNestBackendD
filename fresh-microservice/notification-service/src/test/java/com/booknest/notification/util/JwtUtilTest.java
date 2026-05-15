package com.booknest.notification.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private static final String SECRET = "YourSuperSecretKeyForBookNestPlatformEnsureItIsLongEnough";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
    }

    @Test
    void testValidateToken_Success() {
        String token = Jwts.builder()
                .setSubject("test")
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes()), SignatureAlgorithm.HS256)
                .compact();
        assertTrue(jwtUtil.validateToken(token));
        assertNotNull(jwtUtil.extractAllClaims(token));
    }

    @Test
    void testValidateToken_Invalid() {
        assertFalse(jwtUtil.validateToken("invalid"));
    }

    @Test
    void testValidateToken_Expired() {
        String token = Jwts.builder()
                .setSubject("test")
                .setExpiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes()), SignatureAlgorithm.HS256)
                .compact();
        assertFalse(jwtUtil.validateToken(token));
    }
}
