package com.topleader.topleader.google;

import com.topleader.topleader.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * @author Daniel Slavik
 */
@Sql(scripts = "/sql/google/google-sync-info-test.sql")
class GoogleTokenInfoControllerIT extends IntegrationTest {

    @Test
    @WithMockUser
    void fetchForExistingUserTest() throws Exception {
        mvc.perform(get("/api/latest/google-info"))
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                    "active": true,
                    "status": "ERROR",
                    "lastSync": "2023-08-14T13:00:00Z"
                }
                """)
            )
        ;
    }
    @Test
    @WithMockUser(username = "non-existing-user")
    void fetchForNonExistingUserTest() throws Exception {
        mvc.perform(get("/api/latest/google-info"))
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                    "active": false,
                    "status": null,
                    "lastSync": null
                }
                """)
            )
        ;
    }
}