package com.topleader.topleader.meeting;

import com.topleader.topleader.IntegrationTest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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

    @Test
    @SneakyThrows
    void googleCalendarCallbackRequiresAuth() {
        mvc.perform(get("/login/google").param("code", "test").param("state", "test"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @SneakyThrows
    void googleMeetErrorRequiresAuth() {
        mvc.perform(get("/login/google-meet").param("error", "access_denied"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @SneakyThrows
    void zoomErrorRequiresAuth() {
        mvc.perform(get("/login/zoom").param("error", "access_denied"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @SneakyThrows
    void googleCalendarErrorRequiresAuth() {
        mvc.perform(get("/login/google").param("error", "access_denied"))
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

    // --- OAuth callback with invalid state redirects to error page ---

    @Test
    @SneakyThrows
    @WithMockUser(username = "meet-coach", authorities = {"USER", "COACH"})
    void googleMeetCallbackRejectsInvalidState() {
        mvc.perform(get("/login/google-meet").param("code", "test").param("state", "invalid-state"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("sync-error?provider=gmeet&amp;error=invalid_state")));
    }

    @Test
    @SneakyThrows
    @WithMockUser(username = "meet-coach", authorities = {"USER", "COACH"})
    void zoomCallbackRejectsInvalidState() {
        mvc.perform(get("/login/zoom").param("code", "test").param("state", "invalid-state"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("sync-error?provider=zoom&amp;error=invalid_state")));
    }

    @Test
    @SneakyThrows
    @WithMockUser(username = "meet-coach", authorities = {"USER", "COACH"})
    void googleCalendarCallbackRejectsInvalidState() {
        mvc.perform(get("/login/google").param("code", "test").param("state", "invalid-state"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("sync-error?provider=google-calendar&amp;error=invalid_state")));
    }

    // --- OAuth error parameter redirects to error page ---

    @Test
    @SneakyThrows
    @WithMockUser(username = "meet-coach", authorities = {"USER", "COACH"})
    void googleMeetErrorRedirectsToErrorPage() {
        mvc.perform(get("/login/google-meet").param("error", "access_denied"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("sync-error?provider=gmeet&amp;error=access_denied")));
    }

    @Test
    @SneakyThrows
    @WithMockUser(username = "meet-coach", authorities = {"USER", "COACH"})
    void zoomErrorRedirectsToErrorPage() {
        mvc.perform(get("/login/zoom").param("error", "access_denied"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("sync-error?provider=zoom&amp;error=access_denied")));
    }

    @Test
    @SneakyThrows
    @WithMockUser(username = "meet-coach", authorities = {"USER", "COACH"})
    void googleCalendarErrorRedirectsToErrorPage() {
        mvc.perform(get("/login/google").param("error", "access_denied"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("sync-error?provider=google-calendar&amp;error=access_denied")));
    }

    // --- Calendly token exchange failure redirects to error page ---

    @Test
    @SneakyThrows
    @WithMockUser(username = "meet-coach", authorities = {"USER", "COACH"})
    void calendlyTokenExchangeFailureRedirectsToErrorPage() {
        mvc.perform(get("/login/calendly")
                        .param("code", "invalid-code")
                        .param("username", "meet-coach"))
                .andExpect(status().is3xxRedirection())
                .andExpect(result -> {
                    var location = result.getResponse().getRedirectedUrl();
                    assert location != null && location.contains("sync-error?provider=calendly&error=sync.failed");
                });
    }

    // --- No session state (expired session) redirects to error page ---

    @Test
    @SneakyThrows
    @WithMockUser(username = "meet-coach", authorities = {"USER", "COACH"})
    void googleMeetCallbackRejectsExpiredSession() {
        var session = new MockHttpSession();

        mvc.perform(get("/login/google-meet")
                        .param("code", "test")
                        .param("state", "some-state")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("sync-error?provider=gmeet&amp;error=invalid_state")));
    }

    @Test
    @SneakyThrows
    @WithMockUser(username = "meet-coach", authorities = {"USER", "COACH"})
    void zoomCallbackRejectsExpiredSession() {
        var session = new MockHttpSession();

        mvc.perform(get("/login/zoom")
                        .param("code", "test")
                        .param("state", "some-state")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("sync-error?provider=zoom&amp;error=invalid_state")));
    }

    @Test
    @SneakyThrows
    @WithMockUser(username = "meet-coach", authorities = {"USER", "COACH"})
    void googleCalendarCallbackRejectsExpiredSession() {
        var session = new MockHttpSession();

        mvc.perform(get("/login/google")
                        .param("code", "test")
                        .param("state", "some-state")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("sync-error?provider=google-calendar&amp;error=invalid_state")));
    }

    // --- Token exchange failure redirects to error page ---

    @Test
    @SneakyThrows
    @WithMockUser(username = "meet-coach", authorities = {"USER", "COACH"})
    void googleMeetTokenExchangeFailureRedirectsToErrorPage() {
        var session = new MockHttpSession();
        session.setAttribute("oauth_state_meet", "valid-state");

        mvc.perform(get("/login/google-meet")
                        .param("code", "invalid-code")
                        .param("state", "valid-state")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("sync-error?provider=gmeet&amp;error=token_exchange_failed")));
    }

    @Test
    @SneakyThrows
    @WithMockUser(username = "meet-coach", authorities = {"USER", "COACH"})
    void zoomTokenExchangeFailureRedirectsToErrorPage() {
        var session = new MockHttpSession();
        session.setAttribute("oauth_state_zoom", "valid-state");

        mvc.perform(get("/login/zoom")
                        .param("code", "invalid-code")
                        .param("state", "valid-state")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("sync-error?provider=zoom&amp;error=token_exchange_failed")));
    }

    @Test
    @SneakyThrows
    @WithMockUser(username = "meet-coach", authorities = {"USER", "COACH"})
    void googleCalendarTokenExchangeFailureRedirectsToErrorPage() {
        var session = new MockHttpSession();
        session.setAttribute("oauth_state", "valid-state");

        mvc.perform(get("/login/google")
                        .param("code", "invalid-code")
                        .param("state", "valid-state")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("sync-error?provider=google-calendar&amp;error=token_exchange_failed")));
    }
}
