package com.jobtrack.security;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtService — Tests unitaires")
class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    // 256-bit base64 secret for tests
    private static final String TEST_SECRET =
            "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long EXPIRATION_MS = 86400000L; // 24h

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", EXPIRATION_MS);

        userDetails = User.builder()
                .username("jane@example.com")
                .password("$2a$10$encodedPassword")
                .authorities(Collections.emptyList())
                .build();
    }

    @Test
    @DisplayName("generateToken() — retourne un token non nul")
    void generateToken_returnsNonNullToken() {
        String token = jwtService.generateToken(userDetails);
        assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("extractUsername() — retourne l'email correct")
    void extractUsername_returnsCorrectEmail() {
        String token = jwtService.generateToken(userDetails);
        String extracted = jwtService.extractUsername(token);
        assertThat(extracted).isEqualTo("jane@example.com");
    }

    @Test
    @DisplayName("isTokenValid() — token valide → true")
    void isTokenValid_validToken_returnsTrue() {
        String token = jwtService.generateToken(userDetails);
        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    @DisplayName("isTokenValid() — mauvais utilisateur → false")
    void isTokenValid_wrongUser_returnsFalse() {
        String token = jwtService.generateToken(userDetails);
        UserDetails otherUser = User.builder()
                .username("other@example.com")
                .password("pass")
                .authorities(Collections.emptyList())
                .build();
        assertThat(jwtService.isTokenValid(token, otherUser)).isFalse();
    }

    @Test
    @DisplayName("isTokenExpired() — token expiré → lève ExpiredJwtException")
    void expiredToken_throwsException() {
        // Create a JwtService with 1ms expiration
        JwtService shortJwtService = new JwtService();
        ReflectionTestUtils.setField(shortJwtService, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(shortJwtService, "jwtExpiration", 1L);

        String token = shortJwtService.generateToken(userDetails);

        // Wait for expiry
        try { Thread.sleep(10); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        assertThatThrownBy(() -> shortJwtService.isTokenExpired(token))
                .isInstanceOf(ExpiredJwtException.class);
    }
}
