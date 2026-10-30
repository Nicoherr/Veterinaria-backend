package com.vetsm.backend.security;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SecurityConfigTest {

    @Test
    void corsPermiteLosOrigenesConfiguradosSeparadosPorComas() {
        var source = new SecurityConfig().corsConfigurationSource(
                "https://vetsm.example, https://admin.vetsm.example");
        var request = new MockHttpServletRequest("OPTIONS", "/api/auth/login");
        CorsConfiguration configuration = source.getCorsConfiguration(request);

        assertEquals(
                List.of("https://vetsm.example", "https://admin.vetsm.example"),
                configuration.getAllowedOrigins());
    }
}
