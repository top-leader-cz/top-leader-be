/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.password;

import com.topleader.topleader.IntegrationTest;
import com.topleader.topleader.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * @author Daniel Slavik
 */
@Sql("/sql/user_info/user-info-test.sql")
class PasswordControllerIT extends IntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @WithMockUser()
    void changePasswordRequestValidationTest() throws Exception {

        mvc.perform(post("/api/latest/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {}
                    """)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.oldPassword", is("must not be empty")))
            .andExpect(jsonPath("$.newPassword", is("must not be empty")))
        ;
    }

    @Test
    @WithMockUser()
    void changePasswordInvalidOldPasswordTest() throws Exception {

        mvc.perform(post("/api/latest/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "oldPassword": "invalid",
                        "newPassword": "not-used"
                    }
                    """)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.oldPassword", is("Invalid password")))
        ;
    }

    @Test
    @WithMockUser()
    void changePasswordTest() throws Exception {

        mvc.perform(post("/api/latest/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "oldPassword": "pass",
                        "newPassword": "newPass"
                    }
                    """)
            )
            .andExpect(status().isOk())
        ;

        assertTrue(passwordEncoder.matches("newPass", userRepository.findById("user").orElseThrow().getPassword()));
    }
}
