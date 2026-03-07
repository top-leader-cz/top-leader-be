package com.topleader.topleader.common.ratelimit;

import com.topleader.topleader.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.client.RestClient;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(scripts = "/sql/user_info/session/user-session.sql")
class LoginRateLimitIT extends IntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private LoginAttemptService loginAttemptService;

    @BeforeEach
    public void resetRateLimit() {
        loginAttemptService.resetAttempts("127.0.0.1");
    }

    @Test
    void shouldReturn429AfterMaxFailedAttempts() {
        var client = RestClient.create("http://localhost:" + port);

        IntStream.range(0, 3)
                .map(i -> postLogin(client))
                .forEach(s -> assertThat(s).isEqualTo(401));

        assertThat(postLogin(client)).isEqualTo(429);
    }

    @Test
    void shouldAllowLoginAfterReset() {
        var client = RestClient.create("http://localhost:" + port);

        IntStream.range(0, 3).forEach(i -> postLogin(client));

        assertThat(postLogin(client)).isEqualTo(429);

        loginAttemptService.resetAttempts("127.0.0.1");

        assertThat(postLogin(client)).isEqualTo(401);
    }

    @Test
    void shouldBlockAfterThreeFailedAttemptsWithUsername() {
        var client = RestClient.create("http://localhost:" + port);

        IntStream.range(0, 3).forEach(i ->
                assertThat(postLogin(client, "attacker@evil.com", "wrongpass")).isEqualTo(401));

        assertThat(loginAttemptService.isBlocked("127.0.0.1")).isTrue();

        assertThat(postLogin(client, "attacker@evil.com", "wrongpass")).isEqualTo(429);
    }

    @Test
    void shouldPassUsernameInFailedLoginResponse() {
        var client = RestClient.create("http://localhost:" + port);

        var responseBody = client.post()
                .uri("/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body("username=test@example.com&password=wrongpass")
                .exchange((req, res) -> {
                    assertThat(res.getStatusCode().value()).isEqualTo(401);
                    return new String(res.getBody().readAllBytes());
                });

        assertThat(responseBody).contains("Bad credentials");
    }

    private int postLogin(RestClient client) {
        return postLogin(client, "wrong", "wrong");
    }

    private int postLogin(RestClient client, String username, String password) {
        return client.post()
                .uri("/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body("username=" + username + "&password=" + password)
                .exchange((req, res) -> res.getStatusCode().value());
    }
}
