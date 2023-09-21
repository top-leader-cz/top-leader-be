package com.topleader.topleader.user;

import com.topleader.topleader.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserControllerIT extends IntegrationTest {

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    @WithMockUser(username = "user", authorities = "USER")
    public void addUser() throws Exception {
        mvc.perform(post("/api/latest/user")
                .contentType(MediaType.APPLICATION_JSON)
                          .content("""
                    {
                       "firstName": "Jakub",
                       "lastName": "Svezi",
                       "username":  "jakub.svezi@dummy.com",
                       "password": "test",
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
        ;
    }

    @Test
    @WithMockUser(username = "user", authorities = "USER")
    @Sql(scripts = {"/sql/user/user-test.sql"})
    public void updateUser() throws Exception {
        mvc.perform(put("/api/latest/user/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "status": "PAID"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("user")))
                .andExpect(jsonPath("$.firstName", is("Jakub")))
                .andExpect(jsonPath("$.lastName",  is("Svezi")))
                .andExpect(jsonPath("$.timeZone", is("Europe/Prague")))
                .andExpect(jsonPath("$.status",is("PAID")))
        ;
    }
}
