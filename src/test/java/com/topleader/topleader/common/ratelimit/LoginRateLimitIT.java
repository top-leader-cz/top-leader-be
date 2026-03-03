package com.topleader.topleader.common.ratelimit;

import com.topleader.topleader.IntegrationTest;
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

    @Test
    void shouldReturn429AfterMaxFailedAttempts() {
        var client = RestClient.create("http://localhost:" + port);

        IntStream.range(0, 3)
                .map(i -> postLogin(client))
                .forEach(status -> assertThat(status).isEqualTo(401));

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

    private int postLogin(RestClient client) {
        return client.post()
                .uri("/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body("username=wrong&password=wrong")
                .exchange((req, res) -> res.getStatusCode().value());
    }
}
