package com.topleader.topleader.user.userinsight;

import com.topleader.topleader.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserInsightControllerIT extends IntegrationTest {

    @Test
    @WithMockUser(username = "user", authorities = "USER")
    @Sql(scripts = {"/user_insight/user-insight.sql"})
    void aiQuery() throws Exception {
        mvc.perform(get("/api/latest/user-insight"))
                .andDo(print())
                .andExpect(content().json("""
                          {
                            "leaderShipStyleAnalysis": "leadership-response",
                            "animalSpiritGuide": "animal-response"
                         }
                        """))
                .andExpect(status().isOk());

    }

}