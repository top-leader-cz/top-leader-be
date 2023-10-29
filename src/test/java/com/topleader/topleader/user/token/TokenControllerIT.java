package com.topleader.topleader.user.token;

import com.icegreen.greenmail.util.GreenMailUtil;
import com.topleader.topleader.IntegrationTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TokenControllerIT extends IntegrationTest {

    @Autowired
    private TokenRepository tokenRepository;

    @Test
    @Sql(scripts = {"/sql/token/user-token.sql"})
    public void setPassword() throws Exception {
        mvc.perform(post("/api/public/set-password/test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                       "password": "test-pass"
                     }
                    """))
                .andExpect(status().isOk());

        Assertions.assertThat(tokenRepository.findByTokenAndType("test-token", Token.Type.SET_PASSWORD)).isEmpty();


    }

    @Test
    @Sql(scripts = {"/sql/token/reset-password-link.sql"})
    public void generateResetPasswordLink() throws Exception {
        mvc.perform(post("/api/public/reset-password-link")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                       "username":  "jakub@svezi.cz",
                       "locale": "cs"
                    }
                    """))
                .andExpect(status().isOk())

        ;

        var receivedMessage = greenMail.getReceivedMessages()[0];
        var body = GreenMailUtil.getBody(receivedMessage);
        Assertions.assertThat(greenMail.getReceivedMessages()).hasSize(1);
        Assertions.assertThat(GreenMailUtil.getAddressList(receivedMessage.getFrom())).isEqualTo("top-leader");
        Assertions.assertThat(GreenMailUtil.getAddressList(receivedMessage.getAllRecipients())).isEqualTo("jakub@svezi.cz");
        Assertions.assertThat(receivedMessage.getSubject()).isEqualTo("Instrukce pro obnovení hesla k vašemu účtu v TopLeader");
        Assertions.assertThat(body).contains("Jakub Svezi,").contains("http://app-test-url/#/api/public/set-password/");
        Assertions.assertThat(body).contains("V=C5=A1imli jsme si");

        Assertions.assertThat(tokenRepository.findAll()).isNotEmpty();
    }
}
