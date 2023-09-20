package com.topleader.topleader.user;

import com.topleader.topleader.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserControllerIT extends IntegrationTest {

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    @WithMockUser(username = "user", authorities = "USER")
    public void getEmptyDetailTest() throws Exception {
        var a = passwordEncoder.encode("pass");

        mvc.perform(post("/api/latest/user")
                .contentType(MediaType.APPLICATION_JSON)
                          .content("""
                    {
                       "firstName": "Jakub",
                       "lastName": "Svezi",
                       "email":  "jakub.krhovjak@protonmail.com",
                       "password": "test",
                       "locale": "cs",
                       "timezone": "Europe/Prague",
                       "status": "AUTHORIZED"
                    }
                    """))
                .andExpect(status().isOk())
        ;
    }
}
