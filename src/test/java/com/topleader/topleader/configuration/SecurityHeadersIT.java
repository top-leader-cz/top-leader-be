package com.topleader.topleader.configuration;

import com.topleader.topleader.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(scripts = "/sql/user_info/session/user-session.sql")
class SecurityHeadersIT extends IntegrationTest {

    @Test
    @WithMockUser(username = "user2", authorities = "USER")
    void shouldReturnContentSecurityPolicyHeader() throws Exception {
        mvc.perform(get("/api/latest/user-sessions"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Security-Policy",
                "default-src 'self'; " +
                "script-src 'self' 'unsafe-inline' https://accounts.google.com; " +
                "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
                "font-src 'self' https://fonts.gstatic.com; " +
                "img-src 'self' data: https:; " +
                "connect-src 'self' https://accounts.google.com https://www.googleapis.com; " +
                "frame-src 'self' https://accounts.google.com; " +
                "form-action 'self' https://accounts.google.com"
            ));
    }

    @Test
    @WithMockUser(username = "user2", authorities = "USER")
    void shouldReturnXssProtectionHeader() throws Exception {
        mvc.perform(get("/api/latest/user-sessions"))
            .andExpect(status().isOk())
            .andExpect(header().string("X-XSS-Protection", "1; mode=block"));
    }

    @Test
    @WithMockUser(username = "user2", authorities = "USER")
    void shouldReturnContentTypeOptionsHeader() throws Exception {
        mvc.perform(get("/api/latest/user-sessions"))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Test
    @WithMockUser(username = "user2", authorities = "USER")
    void shouldReturnFrameOptionsHeader() throws Exception {
        mvc.perform(get("/api/latest/user-sessions"))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Frame-Options", "SAMEORIGIN"));
    }

    @Test
    @WithMockUser(username = "user2", authorities = "USER")
    void shouldReturnAllSecurityHeadersTogether() throws Exception {
        mvc.perform(get("/api/latest/user-sessions"))
            .andExpect(status().isOk())
            .andExpect(header().exists("Content-Security-Policy"))
            .andExpect(header().exists("X-XSS-Protection"))
            .andExpect(header().exists("X-Content-Type-Options"))
            .andExpect(header().exists("X-Frame-Options"));
    }

}
