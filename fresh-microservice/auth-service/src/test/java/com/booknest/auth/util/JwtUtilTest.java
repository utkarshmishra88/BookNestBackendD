package com.booknest.auth.util;

import com.booknest.auth.entity.User;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "YourSuperSecretKeyForBookNestPlatformEnsureItIsLongEnough");
        ReflectionTestUtils.setField(jwtUtil, "expirationTime", 3600000L);
    }

    @Test
    void testGenerateAndValidateToken_User() {
        User user = User.builder().userId(1).email("test@ex.com").role("ADMIN").build();
        String token = jwtUtil.generateToken(user);
        assertNotNull(token);
        assertTrue(jwtUtil.validateToken(token));
        assertEquals(1, jwtUtil.extractUserId(token));
        assertEquals("test@ex.com", jwtUtil.extractEmail(token));
        assertEquals("ADMIN", jwtUtil.extractRole(token));
    }

    @Test
    void testGenerateToken_Map() {
        String token = jwtUtil.generateToken(Map.of("userId", 123));
        assertEquals(123, jwtUtil.extractUserId(token));
        assertEquals("123", jwtUtil.extractEmail(token)); // fallback to sub
    }

    @Test
    void testValidateToken_Invalid() {
        assertFalse(jwtUtil.validateToken("invalid-token"));
    }

    @Test
    void testIsTokenExpired() {
        String token = jwtUtil.generateToken(Map.of("userId", 1), -1000L); // expired 1s ago
        assertFalse(jwtUtil.validateToken(token));
    }

    @Test
    void testExtractUserId_NullToken() {
        assertNull(jwtUtil.extractUserId(null));
    }

    @Test
    void testExtractEmail_InvalidToken() {
        assertNull(jwtUtil.extractEmail("garbage"));
    }

    @Test
    void testExtractRole_Null() {
        assertNull(jwtUtil.extractRole(null));
    }

    @Test
    void testGenerateToken_NullUser() {
        String token = jwtUtil.generateToken((User) null);
        assertNotNull(token);
        assertEquals("unknown", jwtUtil.extractClaim(token, Claims::getSubject));
    }

    @Test
    void testGenerateToken_NullMap() {
        String token = jwtUtil.generateToken((Map<String, Object>) null);
        assertNotNull(token);
        assertEquals("unknown", jwtUtil.extractClaim(token, Claims::getSubject));
    }
}
