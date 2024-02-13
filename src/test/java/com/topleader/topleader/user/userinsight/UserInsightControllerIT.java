package com.topleader.topleader.user.userinsight;

import com.topleader.topleader.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static com.topleader.topleader.ai.AiClient.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserInsightControllerIT extends IntegrationTest {

    @Autowired
    ChatClient chatClient;

    @Test
    @WithMockUser(username = "user", authorities = "USER")
    @Sql(scripts = {"/user_insight/user-insight.sql"})
    void getInsight() throws Exception {
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

    @Test
    @WithMockUser(username = "user", authorities = "USER")
    @Sql(scripts = {"/user_insight/user-insight.sql"})
    void generateTips() throws Exception {
        var leaderShipQuery  = String.format(LEADERSHIP_TIP_QUERY, List.of("solver","ideamaker","flexible","responsible","selfBeliever"), List.of("patriotism"), "en");
        Mockito.when(chatClient.call(leaderShipQuery)).thenReturn("leadershipTip-response");

        var personalGrowthQuery  = String.format(PERSONAL_GROWTH_TIP_QUERY, List.of("solver","ideamaker","flexible","responsible","selfBeliever"), List.of("patriotism"), "en");
        Mockito.when(chatClient.call(personalGrowthQuery)).thenReturn("personalGrowthTip-response");

        mvc.perform(get("/api/latest/user-insight/generate-tips"))
                .andDo(print())
                .andExpect(content().json("""
                          {
                            "leadershipTip": "leadershipTip-response",
                            "personalGrowthTip": "personalGrowthTip-response"
                         }
                        """))
                .andExpect(status().isOk());

    }

}