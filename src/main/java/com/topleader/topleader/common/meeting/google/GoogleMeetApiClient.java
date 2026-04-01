package com.topleader.topleader.common.meeting.google;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleMeetApiClient {

    private final RestClient restClient;

    @Value("${google.calendar.events-url:https://www.googleapis.com/calendar/v3/calendars/primary/events}")
    private String calendarEventsUrl;

    public CalendarEventResponse createEventWithMeet(String accessToken, String summary, LocalDateTime start, LocalDateTime end, String requestId) {
        var body = new CalendarEventRequest(
                summary,
                new DateTimeEntry(formatUtc(start)),
                new DateTimeEntry(formatUtc(end)),
                new ConferenceData(new CreateRequest(requestId, new ConferenceSolutionKey("hangoutsMeet"))),
                List.of()
        );

        return restClient.post()
                .uri(calendarEventsUrl + "?conferenceDataVersion=1")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(CalendarEventResponse.class);
    }

    private String formatUtc(LocalDateTime dateTime) {
        return dateTime.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    record CalendarEventRequest(
            String summary,
            DateTimeEntry start,
            DateTimeEntry end,
            ConferenceData conferenceData,
            List<Object> attendees
    ) {
    }

    record DateTimeEntry(String dateTime, String timeZone) {
        DateTimeEntry(String dateTime) {
            this(dateTime, "UTC");
        }
    }

    record ConferenceData(CreateRequest createRequest) {
    }

    record CreateRequest(String requestId, ConferenceSolutionKey conferenceSolutionKey) {
    }

    record ConferenceSolutionKey(String type) {
    }

    public record CalendarEventResponse(String id, String hangoutLink, String htmlLink) {
    }
}
