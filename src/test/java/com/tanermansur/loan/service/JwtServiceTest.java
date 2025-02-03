package com.tanermansur.loan.service;

import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles(profiles = "test")
class JwtServiceTest {
    @InjectMocks
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        Random random = new Random();

        byte[] keyBytes = new byte[32];
        random.nextBytes(keyBytes);

        ReflectionTestUtils.setField(jwtService, "signKey", Keys.hmacShaKeyFor(keyBytes));
    }

    @Test
    void generateToken() {
        String token = jwtService.generateToken("test");

        assertNotNull(token);
        String[] tokenParts = token.split("\\.");
        assertEquals(3, tokenParts.length);
    }

    @Test
    void extractUsername() {
        String token = jwtService.generateToken("test");

        assertEquals("test", jwtService.extractUsername(token));
    }

    @Test
    void extractUsernameWhenInvalidToken() {
        assertThrows(MalformedJwtException.class, () -> jwtService.extractUsername("invalid"));
    }

    @Test
    void extractExpirationDate() {
        String token = jwtService.generateToken("test");

        assertTrue(jwtService.extractExpiration(token).after(new Date()));
    }

    @Test
    void validateTokenWhenValidToken() {
        String token = jwtService.generateToken("test");

        assertTrue(jwtService.validateToken(token, User.withUsername("test").password("password").build()));
    }
}