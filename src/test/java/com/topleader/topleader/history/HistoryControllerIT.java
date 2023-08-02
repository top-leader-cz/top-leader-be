package com.topleader.topleader.history;

import com.topleader.topleader.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * @author Daniel Slavik
 */
@Sql(scripts = {"/sql/history/user-history-test.sql"})
class HistoryControllerIT extends IntegrationTest {

    @Test
    @WithMockUser()
    void findHistoryByType() throws Exception {

        mvc.perform(get("/api/latest/history/STRENGTHS"))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id", is(1)))
            .andExpect(jsonPath("$[0].username", is("user")))
            .andExpect(jsonPath("$[0].type", is("STRENGTHS")))
            .andExpect(jsonPath("$[0].createdAt", is("2023-11-13T16:40:31.313")))
            .andExpect(jsonPath("$[0].data.type", is("STRENGTH_TYPE")))
            .andExpect(jsonPath("$[0].data.strengths", hasSize(2)))
            .andExpect(jsonPath("$[0].data.strengths", hasItems("s1", "s2")))

        ;
    }
}