package com.topleader.topleader.user.token;

import com.topleader.topleader.IntegrationTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TokenControllerIT extends IntegrationTest {

    @Autowired
    private TokenRepository tokenRepository;

    @Test
    @Sql(scripts = {"/sql/token/user-token.sql"})
    public void setPassword() throws Exception {
        mvc.perform(post("/set-password/test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                       "password": "test-pass"
                     }
                    """))
                .andExpect(status().isOk());

        Assertions.assertThat(tokenRepository.findByTokenAndType("test-token", Token.Type.SET_PASSWORD)).isEmpty();


    }
}
