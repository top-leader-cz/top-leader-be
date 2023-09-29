package com.topleader.topleader.user;

import com.icegreen.greenmail.util.GreenMailUtil;
import com.topleader.topleader.IntegrationTest;
import com.topleader.topleader.user.token.Token;
import com.topleader.topleader.user.token.TokenRepository;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.PathAssert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserControllerIT extends IntegrationTest {

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TokenRepository tokenRepository;

    @Test
    @WithMockUser(username = "user", authorities = "ADMIN")
    public void addUser() throws Exception {
        mvc.perform(post("/api/latest/user")
                .contentType(MediaType.APPLICATION_JSON)
                          .content("""
                    {
                       "firstName": "Jakub",
                       "lastName": "Svezi",
                       "username":  "jakub.svezi@dummy.com",
                       "authorities": [ "USER" ],
                       "locale": "cs",
                       "timeZone": "Europe/Prague",
                       "status": "AUTHORIZED"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("jakub.svezi@dummy.com")))
                .andExpect(jsonPath("$.firstName", is("Jakub")))
                .andExpect(jsonPath("$.lastName",  is("Svezi")))
                .andExpect(jsonPath("$.timeZone", is("Europe/Prague")))
                .andExpect(jsonPath("$.status",is("AUTHORIZED")))
                .andExpect(jsonPath("$.authorities", hasItems("USER")))
        ;

        var receivedMessage = greenMail.getReceivedMessages()[0];
        var body = GreenMailUtil.getBody(receivedMessage);
        Assertions.assertThat(greenMail.getReceivedMessages()).hasSize(1);
        Assertions.assertThat(GreenMailUtil.getAddressList(receivedMessage.getAllRecipients())).isEqualTo("jakub.svezi@dummy.com");
        Assertions.assertThat(receivedMessage.getSubject()).isEqualTo("Unlock Your Potential with TopLeader!");
        Assertions.assertThat(body).contains("Jakub Svezi,").contains(" http://app-test-url/#/set-password/");

        Assertions.assertThat(userRepository.findById("jakub.svezi@dummy.com")).isNotEmpty();
    }

    @Test
    @WithMockUser(username = "user", authorities = "USER")
    public void addUser403() throws Exception {

        mvc.perform(post("/api/latest/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                       "firstName": "Jakub",
                       "lastName": "Svezi",
                       "username":  "jakub.svezi@dummy.com",
                       "authorities": [ "USER" ],
                       "locale": "cs",
                       "timeZone": "Europe/Prague",
                       "status": "AUTHORIZED"
                    }
                    """))
                .andExpect(status().is(403))
               ;
    }

    @Test
    @WithMockUser(username = "user", authorities = "ADMIN")
    @Sql(scripts = {"/sql/user/user-test.sql"})
    public void updateUser() throws Exception {
        mvc.perform(put("/api/latest/user/jakub.svezi@dummy.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                       {
                       "firstName": "Jakub1",
                       "lastName": "Svezi2",
                       "authorities": [ "USER" ],
                       "timeZone": "Europe/Paris",
                       "status": "PAID",
                       "locale": "cs"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("jakub.svezi@dummy.com")))
                .andExpect(jsonPath("$.firstName", is("Jakub1")))
                .andExpect(jsonPath("$.lastName",  is("Svezi2")))
                .andExpect(jsonPath("$.timeZone", is("Europe/Paris")))
                .andExpect(jsonPath("$.status",is("PAID")))
        ;

        var receivedMessage = greenMail.getReceivedMessages()[0];
        var body = GreenMailUtil.getBody(receivedMessage);
        Assertions.assertThat(greenMail.getReceivedMessages()).hasSize(1);
        Assertions.assertThat(GreenMailUtil.getAddressList(receivedMessage.getAllRecipients())).isEqualTo("jakub.svezi@dummy.com");
        Assertions.assertThat(receivedMessage.getSubject()).isEqualTo("Unlock Your Potential with TopLeader!");
        Assertions.assertThat(body).contains("Jakub1 Svezi2,").contains(" http://app-test-url/#/set-password/");

        Assertions.assertThat(userRepository.findById("jakub.svezi@dummy.com")).isNotEmpty();

        Assertions.assertThat(userRepository.findById("jakub.svezi@dummy.com")).isNotEmpty();
    }
}
