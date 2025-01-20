package com.topleader.topleader.calendar.calendly;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.topleader.topleader.IntegrationTest;
import com.topleader.topleader.TestUtils;
import com.topleader.topleader.calendar.CalendarSyncInfoRepository;
import com.topleader.topleader.calendar.domain.CalendarSyncInfo;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;


import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Sql("/sql/calendly/calendly-token.sql")
class CalendlyRefreshAccessTokenJobIT extends IntegrationTest {

    @Autowired
    CalendarSyncInfoRepository repository;

    @Test
    @WithMockUser(authorities = "JOB")
    void calendlyRefreshToken() throws Exception {
        mockServer.stubFor(WireMock.post(urlEqualTo("/oauth/token"))
                .withHeader(AUTHORIZATION, equalTo("Basic Ti1LWEROQTQ3Q19hRnYtdWxIZjRCRnNyaDd0T0F6RFNBY1J0S3VNRERYSToyUVJEVGkyME5XV0FCTlpMczQxYmk4cFBGMVI3NEJCTnFPbUxDUzRDRnJz"))
                .withRequestBody(equalTo("grant_type=refresh_token&refresh_token=token&redirect_uri=http%3A%2F%2Flocalhost%3A8080%2Flogin%2Fcalendly"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(TestUtils.readFileAsString("json/coach/calendly-token-response.json"))));

        mvc.perform(MockMvcRequestBuilders.get("/api/protected/jobs/refresh-tokens-calendly"))
                .andExpect(status().isOk());

        Assertions.assertThat(repository.findAll()).extracting(CalendarSyncInfo::getAccessToken, CalendarSyncInfo::getRefreshToken,
                        CalendarSyncInfo::getUsername, CalendarSyncInfo::getOwnerUrl, CalendarSyncInfo::getSyncType, CalendarSyncInfo::getStatus)
                .containsExactly(Assertions.tuple("accessToken", "refreshToken", "coach1",
                        "https://calendly.com/coach1", CalendarSyncInfo.SyncType.CALENDLY, CalendarSyncInfo.Status.OK));
    }
}