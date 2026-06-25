package com.empmngsys.empmngsys.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {
    private static final String SECRET =
            "dGVzdC1zZWNyZXQtZm9yLWp3dC1zZXJ2aWNlLXVuaXQtdGVzdHMtMjU2LWJpdA==";
    private static final String OTHER_SECRET =
            "YW5vdGhlci1zZWNyZXQtZm9yLWp3dC1zZXJ2aWNlLXVuaXQtdGVzdC0yNTYtYg==";

    private JwtService jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 3_600_000L);
        userDetails = User.withUsername("admin")
                .password("ignored")
                .roles("USER", "ADMIN")
                .build();
    }

    @Test
    void generatedTokenCarriesUsername() {
        String token = jwtService.generateToken(userDetails);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsername(token)).isEqualTo("admin");
    }

    @Test
    void validTokenIsAcceptedForMatchingUser() {
        String token = jwtService.generateToken(userDetails);

        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    void tokenIsInvalidForDifferentUser() {
        String token = jwtService.generateToken(userDetails);
        UserDetails other = User.withUsername("user").password("x").roles("USER").build();

        assertThat(jwtService.isTokenValid(token, other)).isFalse();
    }

    @Test
    void expiredTokenThrowsExpiredJwtException() {
        JwtService expiringService = new JwtService(SECRET, -1_000L); // already expired
        String token = expiringService.generateToken(userDetails);

        assertThatThrownBy(() -> expiringService.isTokenValid(token, userDetails))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void tokenSignedWithDifferentSecretIsRejected() {
        String token = jwtService.generateToken(userDetails);
        JwtService otherService = new JwtService(OTHER_SECRET, 3_600_000L);

        assertThatThrownBy(() -> otherService.extractUsername(token))
                .isInstanceOf(SignatureException.class);
    }
}
