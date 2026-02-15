package com.topleader.topleader.common.calendar.calendly;

import com.topleader.topleader.IntegrationTest;
import com.topleader.topleader.TestUtils;
import com.topleader.topleader.common.calendar.CalendarSyncInfoRepository;
import com.topleader.topleader.common.calendar.domain.CalendarSyncInfo;
import okhttp3.mockwebserver.MockResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql("/sql/calendly/calendly-token.sql")
@Disabled
class CalendlyControllerIT extends IntegrationTest {

    @Autowired
    CalendarSyncInfoRepository repository;


    @Test
    @WithMockUser(authorities = "JOB")
    void calendlyLogin() throws Exception {
        stubResponse("/oauth/token", () -> new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(TestUtils.readFileAsString("json/coach/calendly-token-response.json")));

        stubResponse("/users/ownerId", () -> new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "resource": {
                            "email": "coach1"
                          }
                        }
                        """));

        mvc.perform(MockMvcRequestBuilders.get("/login/calendly?code=code&username=coach1"))
                .andExpect(status().is3xxRedirection());

        Assertions.assertThat(repository.findAll()).extracting(CalendarSyncInfo::getAccessToken, CalendarSyncInfo::getRefreshToken,
                        CalendarSyncInfo::getUsername, CalendarSyncInfo::getOwnerUrl, CalendarSyncInfo::getSyncType, CalendarSyncInfo::getStatus)
                .containsExactly(Assertions.tuple("accessToken", "refreshToken", "coach1",
                        "http://localhost:8080/ownerId", CalendarSyncInfo.SyncType.CALENDLY, CalendarSyncInfo.Status.OK));
    }

}
