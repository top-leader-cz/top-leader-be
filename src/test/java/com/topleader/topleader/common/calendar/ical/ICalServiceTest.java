package com.topleader.topleader.common.calendar.ical;

import com.topleader.topleader.common.email.TemplateService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class ICalServiceTest {

    private ICalService iCalService;

    @BeforeEach
    void setUp() {
        var templateService = new TemplateService();
        iCalService = new ICalService(templateService);
        ReflectionTestUtils.setField(iCalService, "defaultFrom", "noreply@topleader.com");
    }

    @Test
    void createCalendarEvent_shouldGenerateValidICalWithTwoAttendees() {
        var start = LocalDateTime.of(2024, 1, 15, 10, 0);
        var end = LocalDateTime.of(2024, 1, 15, 11, 0);

        var result = iCalService.createCalendarEvent(
            start, end,
            "coach@example.com", "John Coach",
            "client@example.com", "Jane Client",
            "Coaching Session",
            "session-123"
        );

        var content = result.toString();

        assertThat(content)
            .contains("BEGIN:VCALENDAR")
            .contains("END:VCALENDAR")
            .contains("VERSION:2.0")
            .contains("METHOD:REQUEST")
            .contains("BEGIN:VEVENT")
            .contains("END:VEVENT")
            .contains("DTSTART:20240115T100000Z")
            .contains("DTEND:20240115T110000Z")
            .contains("UID:session-123")
            .contains("SUMMARY:Coaching Session")
            .contains("ORGANIZER;CN=noreply@topleader.com:mailto:noreply@topleader.com")
            .contains("ATTENDEE;ROLE=REQ-PARTICIPANT;CN=John Coach")
            .contains("mailto:coach@example.com")
            .contains("ATTENDEE;ROLE=REQ-PARTICIPANT;CN=Jane Client")
            .contains("mailto:client@example.com")
            .contains("STATUS:CONFIRMED");
    }

    @Test
    void cancelCalendarEvent_shouldGenerateCancelMethod() {
        var start = LocalDateTime.of(2024, 1, 15, 10, 0);
        var end = LocalDateTime.of(2024, 1, 15, 11, 0);

        var result = iCalService.cancelCalendarEvent(
            start, end,
            "coach@example.com", "John Coach",
            "client@example.com", "Jane Client",
            "Coaching Session",
            "session-123"
        );

        var content = result.toString();

        assertThat(content)
            .contains("METHOD:CANCEL")
            .contains("UID:session-123");
    }

    @Test
    void createCalendarPrivateEvent_shouldGenerateICalWithSingleAttendee() {
        var start = LocalDateTime.of(2024, 2, 20, 14, 30);
        var end = LocalDateTime.of(2024, 2, 20, 15, 30);

        var result = iCalService.createCalendarPrivateEvent(
            start, end,
            "client@example.com", "Jane Client",
            "Private Session",
            "private-456"
        );

        var content = result.toString();

        assertThat(content)
            .contains("BEGIN:VCALENDAR")
            .contains("METHOD:REQUEST")
            .contains("DTSTART:20240220T143000Z")
            .contains("DTEND:20240220T153000Z")
            .contains("UID:private-456")
            .contains("SUMMARY:Private Session")
            .contains("ATTENDEE;ROLE=REQ-PARTICIPANT;CN=Jane Client")
            .contains("mailto:client@example.com")
            .doesNotContain("coach@");
    }

    @Test
    void cancelCalendarPrivateEvent_shouldGenerateCancelMethod() {
        var start = LocalDateTime.of(2024, 2, 20, 14, 30);
        var end = LocalDateTime.of(2024, 2, 20, 15, 30);

        var result = iCalService.cancelCalendarPrivateEvent(
            start, end,
            "client@example.com", "Jane Client",
            "Private Session",
            "private-456"
        );

        var content = result.toString();

        assertThat(content)
            .contains("METHOD:CANCEL")
            .contains("UID:private-456");
    }

    @Test
    void createCalendarEvent_shouldEscapeSpecialCharacters() {
        var start = LocalDateTime.of(2024, 1, 15, 10, 0);
        var end = LocalDateTime.of(2024, 1, 15, 11, 0);

        var result = iCalService.createCalendarEvent(
            start, end,
            "coach@example.com", "John, Coach; Jr.",
            "client@example.com", "Jane\\Client",
            "Session: Test, Event; Special",
            "session-special"
        );

        var content = result.toString();

        assertThat(content)
            .contains("CN=John\\, Coach\\; Jr.")
            .contains("CN=Jane\\\\Client")
            .contains("SUMMARY:Session: Test\\, Event\\; Special");
    }

    @Test
    void createCalendarEvent_shouldContainRequiredICalProperties() {
        var start = LocalDateTime.of(2024, 1, 15, 10, 0);
        var end = LocalDateTime.of(2024, 1, 15, 11, 0);

        var result = iCalService.createCalendarEvent(
            start, end,
            "coach@example.com", "Coach",
            "client@example.com", "Client",
            "Session",
            "session-id"
        );

        var content = result.toString();

        assertThat(content)
            .contains("PRODID:-//TopLeader//Session 1.0//EN")
            .contains("CALSCALE:GREGORIAN")
            .contains("PRIORITY:5")
            .contains("CLASS:PUBLIC")
            .contains("SEQUENCE:0")
            .contains("TRANSP:OPAQUE")
            .contains("RSVP=TRUE")
            .contains("PARTSTAT=NEEDS-ACTION")
            .contains("CUTYPE=INDIVIDUAL");
    }
}
