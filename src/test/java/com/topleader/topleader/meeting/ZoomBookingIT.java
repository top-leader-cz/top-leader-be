package com.topleader.topleader.meeting;

import com.icegreen.greenmail.util.GreenMailUtil;
import com.topleader.topleader.IntegrationTest;
import com.topleader.topleader.coach.availability.CoachAvailability;
import com.topleader.topleader.coach.availability.CoachAvailabilityRepository;
import com.topleader.topleader.common.meeting.MeetingInfoRepository;
import com.topleader.topleader.common.meeting.domain.MeetingInfo;
import com.topleader.topleader.session.scheduled_session.ScheduledSessionRepository;
import lombok.SneakyThrows;
import okhttp3.mockwebserver.MockResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql("/sql/meeting/meeting-zoom-test.sql")
class ZoomBookingIT extends IntegrationTest {

    @Autowired
    private CoachAvailabilityRepository coachAvailabilityRepository;

    @Autowired
    private ScheduledSessionRepository scheduledSessionRepository;

    @Autowired
    private MeetingInfoRepository meetingInfoRepository;

    private String scheduleTimeStr;

    @SneakyThrows
    @BeforeEach
    public void setUp() {
        super.setUp();

        var scheduleTime = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS)
                .plusDays(2).plusHours(9);

        scheduleTimeStr = scheduleTime.atZone(ZoneId.of("UTC")).toString();

        coachAvailabilityRepository.save(
                new CoachAvailability()
                        .setUsername("zoom-coach")
                        .setDateTimeFrom(scheduleTime)
                        .setDateTimeTo(scheduleTime.plusHours(2))
                        .setRecurring(false)
        );

        stubResponse("/zoom/oauth/token", () -> new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("""
                        {
                            "access_token": "zoom-access-token",
                            "refresh_token": "zoom-refresh-token",
                            "token_type": "Bearer",
                            "expires_in": 3600
                        }
                        """));

        stubResponse("/zoom/v2/users/me/meetings", () -> new MockResponse()
                .setResponseCode(201)
                .addHeader("Content-Type", "application/json")
                .setBody("""
                        {
                            "id": 98765,
                            "join_url": "https://zoom.us/j/98765?pwd=abc123",
                            "start_url": "https://zoom.us/s/98765?zak=xyz",
                            "topic": "TopLeader Session"
                        }
                        """));
    }

    @Test
    @SneakyThrows
    @WithMockUser("zoom-client")
    void bookingWithZoomLinkTest() {
        mvc.perform(post("/api/latest/coaches/zoom-coach/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {"time": "%s"}
                                """, scheduleTimeStr)))
                .andExpect(status().isOk());

        assertThat(scheduledSessionRepository.findAll(), hasSize(1));

        var messages = greenMail.getReceivedMessages();
        Assertions.assertThat(messages).hasSizeGreaterThanOrEqualTo(2);

        var coachBody = GreenMailUtil.getBody(messages[0]);
        var clientBody = GreenMailUtil.getBody(messages[1]);
        Assertions.assertThat(coachBody).contains("https://zoom.us/j/98765?pwd=abc123");
        Assertions.assertThat(clientBody).contains("https://zoom.us/j/98765?pwd=abc123");
    }

    @Test
    @SneakyThrows
    @WithMockUser("zoom-client")
    void bookingWithZoomApiFailureTest() {
        stubResponse("/zoom/oauth/token", () -> new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("""
                        {
                            "access_token": "zoom-token",
                            "token_type": "Bearer",
                            "expires_in": 3600
                        }
                        """));

        stubResponse("/zoom/v2/users/me/meetings", () -> new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));

        mvc.perform(post("/api/latest/coaches/zoom-coach/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {"time": "%s"}
                                """, scheduleTimeStr)))
                .andExpect(status().isOk());

        assertThat(scheduledSessionRepository.findAll(), hasSize(1));

        var info = meetingInfoRepository.findByUsername("zoom-coach").orElseThrow();
        assertThat(info.getStatus(), is(MeetingInfo.Status.WARN));
    }
}
