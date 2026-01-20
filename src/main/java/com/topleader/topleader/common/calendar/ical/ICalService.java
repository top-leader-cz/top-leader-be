package com.topleader.topleader.common.calendar.ical;

import com.topleader.topleader.common.email.TemplateService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ICalService {

    private static final DateTimeFormatter ICAL_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
    private static final String METHOD_REQUEST = "REQUEST";
    private static final String METHOD_CANCEL = "CANCEL";
    private static final String EVENT_TEMPLATE = "templates/ical/event.ics";
    private static final String PRIVATE_EVENT_TEMPLATE = "templates/ical/private-event.ics";

    private final TemplateService templateService;

    @Value("${top-leader.default-from}")
    private String defaultFrom;

    public ICalEvent createCalendarEvent(
        LocalDateTime start,
        LocalDateTime end,
        String coach,
        String coachName,
        String client,
        String clientName,
        String eventName,
        String eventId
    ) {
        var event = buildCalendarEvent(start, end, coach, coachName, client, clientName, eventName, eventId, METHOD_REQUEST);
        log.debug(event.toString());
        return event;
    }

    public ICalEvent cancelCalendarEvent(
        LocalDateTime start,
        LocalDateTime end,
        String coach,
        String coachName,
        String client,
        String clientName,
        String eventName,
        String eventId
    ) {
        return buildCalendarEvent(start, end, coach, coachName, client, clientName, eventName, eventId, METHOD_CANCEL);
    }

    public ICalEvent createCalendarPrivateEvent(
        LocalDateTime start,
        LocalDateTime end,
        String client,
        String clientName,
        String eventName,
        String eventId
    ) {
        var event = buildPrivateCalendarEvent(start, end, client, clientName, eventName, eventId, METHOD_REQUEST);
        log.debug(event.toString());
        return event;
    }

    public ICalEvent cancelCalendarPrivateEvent(
        LocalDateTime start,
        LocalDateTime end,
        String client,
        String clientName,
        String eventName,
        String eventId
    ) {
        return buildPrivateCalendarEvent(start, end, client, clientName, eventName, eventId, METHOD_CANCEL);
    }

    private ICalEvent buildCalendarEvent(
        LocalDateTime start,
        LocalDateTime end,
        String coach,
        String coachName,
        String client,
        String clientName,
        String eventName,
        String eventId,
        String method
    ) {
        var params = Map.<String, Object>of(
            "method", method,
            "startDate", start.format(ICAL_DATE_FORMAT),
            "endDate", end.format(ICAL_DATE_FORMAT),
            "eventId", eventId,
            "eventName", escapeText(eventName),
            "organizer", defaultFrom,
            "coach", coach,
            "coachName", escapeText(coachName),
            "client", client,
            "clientName", escapeText(clientName)
        );
        var content = templateService.getMessage(params, EVENT_TEMPLATE);
        return new ICalEvent(content);
    }

    private ICalEvent buildPrivateCalendarEvent(
        LocalDateTime start,
        LocalDateTime end,
        String client,
        String clientName,
        String eventName,
        String eventId,
        String method
    ) {
        var params = Map.<String, Object>of(
            "method", method,
            "startDate", start.format(ICAL_DATE_FORMAT),
            "endDate", end.format(ICAL_DATE_FORMAT),
            "eventId", eventId,
            "eventName", escapeText(eventName),
            "organizer", defaultFrom,
            "client", client,
            "clientName", escapeText(clientName)
        );
        var content = templateService.getMessage(params, PRIVATE_EVENT_TEMPLATE);
        return new ICalEvent(content);
    }

    private String escapeText(String text) {
        if (text == null) {
            return "";
        }
        return text
            .replace("\\", "\\\\")
            .replace(",", "\\,")
            .replace(";", "\\;")
            .replace("\n", "\\n");
    }
}
