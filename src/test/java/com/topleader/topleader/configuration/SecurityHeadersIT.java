package com.topleader.topleader.configuration;

import com.topleader.topleader.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SecurityHeadersIT extends IntegrationTest {

    @Test
    @WithMockUser(username = "test@test.com", authorities = "USER")
    void shouldReturnContentSecurityPolicyHeader() throws Exception {
        mvc.perform(get("/api/latest/user/context"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Security-Policy",
                "default-src 'self'; " +
                "script-src 'self'; " +
                "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
                "font-src 'self' https://fonts.gstatic.com; " +
                "img-src 'self' data: https:; " +
                "connect-src 'self'"
            ));
    }

    @Test
    @WithMockUser(username = "test@test.com", authorities = "USER")
    void shouldReturnXssProtectionHeader() throws Exception {
        mvc.perform(get("/api/latest/user/context"))
            .andExpect(status().isOk())
            .andExpect(header().string("X-XSS-Protection", "1; mode=block"));
    }

    @Test
    @WithMockUser(username = "test@test.com", authorities = "USER")
    void shouldReturnContentTypeOptionsHeader() throws Exception {
        mvc.perform(get("/api/latest/user/context"))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Test
    @WithMockUser(username = "test@test.com", authorities = "USER")
    void shouldReturnFrameOptionsHeader() throws Exception {
        mvc.perform(get("/api/latest/user/context"))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Frame-Options", "SAMEORIGIN"));
    }

    @Test
    @WithMockUser(username = "test@test.com", authorities = "USER")
    void shouldReturnAllSecurityHeadersTogether() throws Exception {
        mvc.perform(get("/api/latest/user/context"))
            .andExpect(status().isOk())
            .andExpect(header().exists("Content-Security-Policy"))
            .andExpect(header().exists("X-XSS-Protection"))
            .andExpect(header().exists("X-Content-Type-Options"))
            .andExpect(header().exists("X-Frame-Options"));
    }

    @Test
    void shouldReturnSecurityHeadersEvenOnPublicEndpoints() throws Exception {
        mvc.perform(get("/actuator/health"))
            .andExpect(status().isOk())
            .andExpect(header().exists("Content-Security-Policy"))
            .andExpect(header().exists("X-XSS-Protection"))
            .andExpect(header().exists("X-Content-Type-Options"))
            .andExpect(header().exists("X-Frame-Options"));
    }
}
