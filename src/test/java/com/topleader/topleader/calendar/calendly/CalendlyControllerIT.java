package com.topleader.topleader.calendar.calendly;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.topleader.topleader.IntegrationTest;
import com.topleader.topleader.TestUtils;
import com.topleader.topleader.calendar.CalendarSyncInfoRepository;
import com.topleader.topleader.calendar.domain.CalendarSyncInfo;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql("/sql/calendly/calendly-token.sql")
class CalendlyControllerIT extends IntegrationTest {

    @Autowired
    CalendarSyncInfoRepository repository;

    @SneakyThrows
    @BeforeEach
    public void setUp() {
        super.setUp();
        mockServer.stubFor(WireMock.post(urlEqualTo("/oauth/token"))
                .withHeader(AUTHORIZATION, equalTo("Basic Ti1LWEROQTQ3Q19hRnYtdWxIZjRCRnNyaDd0T0F6RFNBY1J0S3VNRERYSToyUVJEVGkyME5XV0FCTlpMczQxYmk4cFBGMVI3NEJCTnFPbUxDUzRDRnJz"))
//                .withRequestBody(equalTo("grant_type%3Dauthorization_code%26code%3Dcode%26redirect_uri%3Dhttp%3A%2F%2Flocalhost%3A8080%2Flogin%2Fcalendly%3Fusername%3Dcoach1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(TestUtils.readFileAsString("json/coach/calendly-token-response.json"))));

    }


    @Test
    @WithMockUser(authorities = "JOB")
    void calendlyLogin() throws Exception {
        mockServer.stubFor(WireMock.get(urlEqualTo("/users/ownerId"))
                .withHeader(AUTHORIZATION, equalTo("Bearer accessToken"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "resource": {
                                    "email": "coach1"
                                  }
                                }
                                """)));

        mvc.perform(MockMvcRequestBuilders.get("/login/calendly?code=code&username=coach1"))
                .andExpect(status().is3xxRedirection());

        Assertions.assertThat(repository.findAll()).extracting(CalendarSyncInfo::getAccessToken, CalendarSyncInfo::getRefreshToken,
                        c -> c.getId().getUsername(), CalendarSyncInfo::getOwnerUrl, CalendarSyncInfo::getSyncType, CalendarSyncInfo::getStatus)
                .containsExactly(Assertions.tuple("accessToken", "refreshToken", "coach1",
                        "http://localhost:8080/ownerId", CalendarSyncInfo.SyncType.CALENDLY, CalendarSyncInfo.Status.OK));
    }

    @Test
    @WithMockUser("coach1")
    void calendlyLoginEmailDoNotMatch() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/api/latest/calendar")
                        .contentType("application/json")
                        .content("""    
                                                      {
                                                          "email": "matched@email.com",
                                                          "syncType": "CALENDLY"
                                                      }
                                """))
                .andExpect(status().isOk());

        mockServer.stubFor(WireMock.get(urlEqualTo("/users/ownerId"))
                .withHeader(AUTHORIZATION, equalTo("Bearer accessToken"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "resource": {
                                    "email": "matched@email.com"
                                  }
                                }
                                """)));

        mvc.perform(MockMvcRequestBuilders.get("/login/calendly?code=code"))
                .andExpect(status().is3xxRedirection());

        Assertions.assertThat(repository.findAll()).extracting(CalendarSyncInfo::getAccessToken, CalendarSyncInfo::getRefreshToken,
                        c -> c.getId().getUsername(), CalendarSyncInfo::getOwnerUrl, CalendarSyncInfo::getSyncType, CalendarSyncInfo::getStatus, CalendarSyncInfo::getEmail)
                .containsExactly(Assertions.tuple("accessToken", "refreshToken", "coach1",
                        "http://localhost:8080/ownerId", CalendarSyncInfo.SyncType.CALENDLY, CalendarSyncInfo.Status.OK, "matched@email.com"));
    }




}