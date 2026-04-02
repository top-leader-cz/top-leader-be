package com.topleader.topleader.meeting;

import com.topleader.topleader.IntegrationTest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql("/sql/meeting/meeting-test.sql")
class OAuthSecurityIT extends IntegrationTest {

    // --- Unauthenticated access returns 401 ---

    @Test
    @SneakyThrows
    void googleMeetInitiateRequiresAuth() {
        mvc.perform(get("/login/google-meet"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @SneakyThrows
    void googleMeetCallbackRequiresAuth() {
        mvc.perform(get("/login/google-meet").param("code", "test").param("state", "test"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @SneakyThrows
    void zoomInitiateRequiresAuth() {
        mvc.perform(get("/login/zoom"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @SneakyThrows
    void zoomCallbackRequiresAuth() {
        mvc.perform(get("/login/zoom").param("code", "test").param("state", "test"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @SneakyThrows
    void googleCalendarInitiateRequiresAuth() {
        mvc.perform(get("/login/google"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @SneakyThrows
    void calendlyCallbackRequiresAuth() {
        mvc.perform(get("/login/calendly").param("code", "test").param("username", "someone"))
                .andExpect(status().isUnauthorized());
    }

    // --- Authenticated access works (redirects to OAuth provider) ---

    @Test
    @SneakyThrows
    @WithMockUser(username = "meet-coach", authorities = {"USER", "COACH"})
    void googleMeetInitiateRedirectsWhenAuthenticated() {
        mvc.perform(get("/login/google-meet"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @SneakyThrows
    @WithMockUser(username = "meet-coach", authorities = {"USER", "COACH"})
    void zoomInitiateRedirectsWhenAuthenticated() {
        mvc.perform(get("/login/zoom"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @SneakyThrows
    @WithMockUser(username = "meet-coach", authorities = {"USER", "COACH"})
    void googleCalendarInitiateRedirectsWhenAuthenticated() {
        mvc.perform(get("/login/google"))
                .andExpect(status().is3xxRedirection());
    }

    // --- Calendly username mismatch ---

    @Test
    @SneakyThrows
    @WithMockUser(username = "meet-coach", authorities = {"USER", "COACH"})
    void calendlyRejectsUsernameMismatch() {
        mvc.perform(get("/login/calendly")
                        .param("code", "test-code")
                        .param("username", "another-user"))
                .andExpect(status().is3xxRedirection())
                .andExpect(result -> {
                    var location = result.getResponse().getRedirectedUrl();
                    assert location != null && location.contains("auth.mismatch");
                });
    }

    // --- OAuth callback with invalid state returns 400 ---

    @Test
    @SneakyThrows
    @WithMockUser(username = "meet-coach", authorities = {"USER", "COACH"})
    void googleMeetCallbackRejectsInvalidState() {
        mvc.perform(get("/login/google-meet").param("code", "test").param("state", "invalid-state"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    @WithMockUser(username = "meet-coach", authorities = {"USER", "COACH"})
    void zoomCallbackRejectsInvalidState() {
        mvc.perform(get("/login/zoom").param("code", "test").param("state", "invalid-state"))
                .andExpect(status().isBadRequest());
    }
}
