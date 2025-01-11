package com.topleader.topleader.coach.list;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.topleader.topleader.IntegrationTest;
import com.topleader.topleader.TestUtils;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Sql("/sql/coach/coach-list-test.sql")
class CoachAvailabilityJobControllerIT extends IntegrationTest {

    @Autowired
    CoachListViewRepository coachListViewRepository;

    ZonedDateTime testedTime = ZonedDateTime.of(LocalDateTime.of(
            LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY)),
            LocalTime.of(9, 0)
    ), ZoneId.of("UTC"));

    ZonedDateTime startTime = testedTime.minusHours(1);
    ZonedDateTime endTime = testedTime;

    @SneakyThrows
    @BeforeEach
    public void setUp() {
        super.setUp();

        mockServer.stubFor(WireMock.get(urlMatching("/scheduled_events\\?user=.*"))
                .withHeader(AUTHORIZATION, equalTo("Bearer accessToken"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(String.format("""
                                {
                                    "collection": [
                                        {
                                            "id": "event1",
                                            "start_time": "%s",
                                            "end_time": "%s"
                                        },
                                        {
                                            "id": "event2",
                                            "start_time": "2023-08-14T13:00:00Z",
                                            "end_time": "2023-08-14T14:00:00Z"
                                        }
                                    ]
                                }
                                """, startTime, endTime))));


        mockServer.stubFor(WireMock.post(urlEqualTo("/oauth/token"))
                .withHeader(AUTHORIZATION, equalTo("Basic Ti1LWEROQTQ3Q19hRnYtdWxIZjRCRnNyaDd0T0F6RFNBY1J0S3VNRERYSToyUVJEVGkyME5XV0FCTlpMczQxYmk4cFBGMVI3NEJCTnFPbUxDUzRDRnJz"))
                .withRequestBody(equalTo("grant_type=refresh_token&refresh_token=token&redirect_uri=http%3A%2F%2Flocalhost%3A8080%2Flogin%2Fcalendly"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(TestUtils.readFileAsString("json/coach/calendly-token-response.json"))));
    }

    @Test
    @WithMockUser(authorities = "JOB")
    void testAvailabilityJob() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/api/protected/jobs/set-free-slots"))
                .andExpect(status().isOk());

        Assertions.assertThat(coachListViewRepository.findAll())
                .extracting(CoachListView::getUsername, CoachListView::isFreeSlots)
                .containsExactlyInAnyOrder(
                        new Tuple("coach1", true),
                        new Tuple("coach2", false),
                        new Tuple("coach3", false),
                        new Tuple("coach4", true)
         );
    }
}