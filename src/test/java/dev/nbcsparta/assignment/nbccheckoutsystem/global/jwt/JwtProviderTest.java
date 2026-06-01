package dev.nbcsparta.assignment.nbccheckoutsystem.global.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Base64;

import org.junit.jupiter.api.Test;

class JwtProviderTest {

    private final JwtProvider jwtProvider = new JwtProvider(
            Base64.getEncoder().encodeToString("nbc-checkout-system-jwt-secret-key-for-tests".getBytes()),
            3_600_000L
    );

    @Test
    void createTokenContainsMemberIdAndValidatesToken() {
        String token = jwtProvider.createToken(1L, "member@example.com");

        assertThat(jwtProvider.validate(token)).isTrue();
        assertThat(jwtProvider.getMemberId(token)).isEqualTo(1L);
    }

    @Test
    void validateReturnsFalseForInvalidToken() {
        assertThat(jwtProvider.validate("invalid.token.value")).isFalse();
    }
}
