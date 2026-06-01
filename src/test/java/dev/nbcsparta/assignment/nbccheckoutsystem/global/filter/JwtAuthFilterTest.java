package dev.nbcsparta.assignment.nbccheckoutsystem.global.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Base64;

import dev.nbcsparta.assignment.nbccheckoutsystem.global.jwt.JwtProvider;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

class JwtAuthFilterTest {

    private final JwtProvider jwtProvider = new JwtProvider(
            Base64.getEncoder().encodeToString("nbc-checkout-system-jwt-secret-key-for-tests".getBytes()),
            3_600_000L
    );

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validBearerTokenSetsMemberIdAsAuthenticationPrincipal() throws ServletException, IOException {
        JwtAuthFilter filter = new JwtAuthFilter(jwtProvider);
        String token = jwtProvider.createToken(7L, "member@example.com");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo(7L);
    }

    @Test
    void invalidBearerTokenReturnsUnauthorized() throws ServletException, IOException {
        JwtAuthFilter filter = new JwtAuthFilter(jwtProvider);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid.token.value");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(401);
    }
}
