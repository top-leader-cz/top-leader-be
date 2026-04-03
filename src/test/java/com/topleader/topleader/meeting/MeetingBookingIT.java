package com.topleader.topleader.meeting;

import com.icegreen.greenmail.util.GreenMailUtil;
import com.topleader.topleader.IntegrationTest;
import com.topleader.topleader.coach.availability.CoachAvailability;
import com.topleader.topleader.coach.availability.CoachAvailabilityRepository;
import com.topleader.topleader.common.meeting.MeetingInfoRepository;
import com.topleader.topleader.common.meeting.domain.MeetingInfo;
import com.topleader.topleader.common.notification.NotificationRepository;
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

import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql("/sql/meeting/meeting-test.sql")
class MeetingBookingIT extends IntegrationTest {

    @Autowired
    private CoachAvailabilityRepository coachAvailabilityRepository;

    @Autowired
    private ScheduledSessionRepository scheduledSessionRepository;

    @Autowired
    private MeetingInfoRepository meetingInfoRepository;

    @Autowired
    private NotificationRepository notificationRepository;

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
                        .setUsername("meet-coach")
                        .setDateTimeFrom(scheduleTime)
                        .setDateTimeTo(scheduleTime.plusHours(2))
                        .setRecurring(false)
        );

        // Stub Google token refresh
        stubResponse("/token", () -> new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("""
                        {
                            "access_token": "fresh-access-token",
                            "token_type": "Bearer",
                            "expires_in": 3600
                        }
                        """));

        // Stub Google Calendar event creation with Meet link
        stubResponse("/calendar/v3/calendars/primary/events", () -> new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("""
                        {
                            "id": "event123",
                            "hangoutLink": "https://meet.google.com/abc-defg-hij",
                            "htmlLink": "https://calendar.google.com/event?id=event123"
                        }
                        """));
    }

    @Test
    @SneakyThrows
    @WithMockUser("meet-client")
    void bookingWithMeetLinkTest() {
        mvc.perform(post("/api/latest/coaches/meet-coach/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {"time": "%s"}
                                """, scheduleTimeStr)))
                .andExpect(status().isOk());

        assertThat(scheduledSessionRepository.findAll(), hasSize(1));

        // Both coach and client should receive emails
        var messages = greenMail.getReceivedMessages();
        Assertions.assertThat(messages).hasSizeGreaterThanOrEqualTo(2);

        // Check meet link is in emails
        var coachBody = GreenMailUtil.getBody(messages[0]);
        var clientBody = GreenMailUtil.getBody(messages[1]);
        Assertions.assertThat(coachBody).contains("https://meet.google.com/abc-defg-hij");
        Assertions.assertThat(clientBody).contains("https://meet.google.com/abc-defg-hij");

        // Verify attendees were sent in the Calendar API request
        var calendarRequest = findRequest("/calendar/v3/calendars/primary/events");
        var requestBody = calendarRequest.getBody().readUtf8();
        Assertions.assertThat(requestBody).contains("meet-coach@gmail.com");
        Assertions.assertThat(requestBody).contains("meet-client@example.com");
    }

    @SneakyThrows
    private RecordedRequest findRequest(String pathPrefix) {
        return Stream.generate(() -> takeRequestQuietly(mockServer))
                .takeWhile(Objects::nonNull)
                .filter(r -> r.getPath() != null && r.getPath().startsWith(pathPrefix))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No request found for path: " + pathPrefix));
    }

    @SneakyThrows
    private static RecordedRequest takeRequestQuietly(MockWebServer server) {
        return server.takeRequest(1, TimeUnit.SECONDS);
    }

    @Test
    @SneakyThrows
    @WithMockUser("meet-client")
    void bookingWithoutMeetProviderTest() {
        // Disconnect meeting provider
        meetingInfoRepository.deleteByUsername("meet-coach");

        mvc.perform(post("/api/latest/coaches/meet-coach/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {"time": "%s"}
                                """, scheduleTimeStr)))
                .andExpect(status().isOk());

        assertThat(scheduledSessionRepository.findAll(), hasSize(1));

        // Emails should be sent without meet link
        var messages = greenMail.getReceivedMessages();
        Assertions.assertThat(messages).hasSizeGreaterThanOrEqualTo(2);
        var coachBody = GreenMailUtil.getBody(messages[0]);
        Assertions.assertThat(coachBody).doesNotContain("meet.google.com");
    }

    @Test
    @SneakyThrows
    @WithMockUser("meet-client")
    void bookingWithAutoGenerateDisabledTest() {
        meetingInfoRepository.updateAutoGenerate("meet-coach", false);

        mvc.perform(post("/api/latest/coaches/meet-coach/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {"time": "%s"}
                                """, scheduleTimeStr)))
                .andExpect(status().isOk());

        assertThat(scheduledSessionRepository.findAll(), hasSize(1));

        var messages = greenMail.getReceivedMessages();
        Assertions.assertThat(messages).hasSizeGreaterThanOrEqualTo(2);
        var coachBody = GreenMailUtil.getBody(messages[0]);
        Assertions.assertThat(coachBody).doesNotContain("meet.google.com");
    }

    @Test
    @SneakyThrows
    @WithMockUser("meet-client")
    void bookingWithGoogleApiFailureTest() {
        // Stub Google API to fail
        stubResponse("/token", () -> new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("""
                        {
                            "access_token": "fresh-token",
                            "token_type": "Bearer",
                            "expires_in": 3600
                        }
                        """));

        stubResponse("/calendar/v3/calendars/primary/events", () -> new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));

        mvc.perform(post("/api/latest/coaches/meet-coach/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {"time": "%s"}
                                """, scheduleTimeStr)))
                .andExpect(status().isOk());

        // Session should still be created
        assertThat(scheduledSessionRepository.findAll(), hasSize(1));

        // Meet info status should escalate to WARN
        var info = meetingInfoRepository.findByUsername("meet-coach").orElseThrow();
        assertThat(info.getStatus(), is(MeetingInfo.Status.WARN));

        // Notification should be sent to coach
        var notifications = notificationRepository.findAll();
        Assertions.assertThat(notifications).isNotEmpty();
    }

    @Test
    @SneakyThrows
    @WithMockUser("meet-client")
    void bookingWithErrorStatusSkipsLinkGenerationTest() {
        // Set status to ERROR — link generation should be skipped
        var info = meetingInfoRepository.findByUsername("meet-coach").orElseThrow();
        meetingInfoRepository.save(info.setStatus(MeetingInfo.Status.ERROR));

        mvc.perform(post("/api/latest/coaches/meet-coach/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {"time": "%s"}
                                """, scheduleTimeStr)))
                .andExpect(status().isOk());

        assertThat(scheduledSessionRepository.findAll(), hasSize(1));

        // Emails should not contain meet link
        var messages = greenMail.getReceivedMessages();
        Assertions.assertThat(messages).hasSizeGreaterThanOrEqualTo(2);
        var coachBody = GreenMailUtil.getBody(messages[0]);
        Assertions.assertThat(coachBody).doesNotContain("meet.google.com");
    }
}
