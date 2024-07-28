package com.topleader.topleader.user.userinsight;

import com.topleader.topleader.IntegrationTest;
import com.topleader.topleader.ai.AiPrompt;
import com.topleader.topleader.ai.AiPromptService;
import com.topleader.topleader.user.userinfo.UserInfoRepository;
import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserInsightControllerIT extends IntegrationTest {

    @Autowired
    ChatModel chatClient;

    @Autowired
    UserInfoRepository userInfoRepository;

    @Autowired
    UserInsightRepository userInsightRepository;

    @Autowired
    AiPromptService aiPromptService;

    @Test
    @WithMockUser(username = "user", authorities = "USER")
    @Sql(scripts = {"/user_insight/user-insight.sql"})
    void getInsight() throws Exception {
        mvc.perform(get("/api/latest/user-insight"))
                .andDo(print())
                .andExpect(content().json("""
                          {
                          "personalGrowthTip":{"text":null,"isPending":false},
                          "leaderShipStyle":{"text":"leadership-response","isPending":false},
                          "leaderPersona":{"text": "world-leader-persona","isPending":false},
                          "animalSpirit":{"text":"animal-response","isPending":false},
                          "leadershipTip":{"text":null,"isPending":false}
                          }
                        """))
                .andExpect(status().isOk());

    }

    @Test
    @WithMockUser(username = "user", authorities = "USER")
    @Sql(scripts = {"/user_insight/user-insight.sql", "/user_insight/ai-prompt.sql"})
    void generateTips() throws Exception {
        var leaderShipQuery = String.format(aiPromptService.getPrompt(AiPrompt.PromptType.LEADERSHIP_TIP), List.of("solver", "ideamaker", "flexible", "responsible", "selfBeliever"), List.of("patriotism"), "English");
        Mockito.when(chatClient.call(leaderShipQuery)).thenReturn("leadershipTip-response");

        mvc.perform(get("/api/latest/user-insight/generate-tips"))
                .andDo(print())
                .andExpect(status().isOk());

        Assertions.assertThat(userInsightRepository.findAll())
                .extracting(UserInsight::getLeadershipTip, UserInsight::getPersonalGrowthTip)
                .containsExactly(new Tuple("leadershipTip-response", null));

    }

    @Test
    @WithMockUser(username = "user", authorities = "USER")
    @Sql(scripts = {"/user_insight/user-insight.sql", "/user_insight/ai-prompt.sql"})
    void generateTipsNoStrengthsAndValues() throws Exception {
        userInfoRepository.deleteAll();
        var leaderShipQuery = String.format(aiPromptService.getPrompt(AiPrompt.PromptType.LEADERSHIP_TIP), List.of("solver", "ideamaker", "flexible", "responsible", "selfBeliever"), List.of("patriotism"), "en");
        Mockito.when(chatClient.call(leaderShipQuery)).thenReturn("leadershipTip-response");

        mvc.perform(get("/api/latest/user-insight/generate-tips"))
                .andExpect(status().isOk());


        Assertions.assertThat(userInsightRepository.findAll())
                .extracting(UserInsight::getLeadershipTip, UserInsight::getPersonalGrowthTip)
                .containsExactly(new Tuple(null, null));

    }

}