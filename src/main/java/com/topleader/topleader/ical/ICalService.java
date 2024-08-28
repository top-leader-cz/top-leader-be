/*
 * Copyright (c) 2024 Price f(x), s.r.o.
 */
package com.topleader.topleader.ical;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import lombok.extern.slf4j.Slf4j;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.parameter.CuType;
import net.fortuna.ical4j.model.parameter.PartStat;
import net.fortuna.ical4j.model.parameter.Rsvp;
import net.fortuna.ical4j.model.property.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.Role;


/**
 * @author Daniel Slavik
 */
@Slf4j
@Service
public class ICalService {

    @Value("${top-leader.default-from}")
    private String defaultFrom;

    private static final String MAILTO = "mailto:";

    public Calendar createCalendarEvent(
        LocalDateTime start,
        LocalDateTime end,
        String coach,
        String coachName,
        String client,
        String clientName,
        String eventName,
        String eventId
    ) {
        final var calendar = baseCalendarEvent(start, end, coach, coachName, client, clientName, eventName, eventId, Method.VALUE_REQUEST);
        log.debug(calendar.toString());
        return calendar;
    }

    public Calendar cancelCalendarEvent(
        LocalDateTime start,
        LocalDateTime end,
        String coach,
        String coachName,
        String client,
        String clientName,
        String eventName,
        String eventId
    ) {
        return baseCalendarEvent(start, end, coach, coachName, client, clientName, eventName, eventId, Method.VALUE_CANCEL);
    }

    public Calendar baseCalendarEvent(
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

        // Create a TimeZone
        final var registry = TimeZoneRegistryFactory.getInstance().createRegistry();
        final var timezone = registry.getTimeZone("UTC");
        final var tz = timezone.getVTimeZone();

        return new Calendar().withProdId("-//Ben Fortuna//iCal4j 1.0//EN")
            .withDefaults()
            .withProperty(new Method(method))
            .withComponent(
                new VEvent(ZonedDateTime.of(start, ZoneOffset.UTC), ZonedDateTime.of(end, ZoneOffset.UTC), eventName)
                    .withProperty(tz.getProperty(Property.TZID).orElseThrow())
                    .withProperty(new Uid(eventId))
                    .withProperty(
                        new Organizer(URI.create(MAILTO + defaultFrom))
                            .withParameter(new Cn(defaultFrom))
                            .getFluentTarget())
                    .withProperty(
                        new Attendee(URI.create(MAILTO + coach))
                            .withParameter(Role.REQ_PARTICIPANT)
                            .withParameter(new Cn(coachName))
                            .withParameter(new CuType(CuType.INDIVIDUAL.getValue()))
                            .withParameter(new Role(Role.REQ_PARTICIPANT.getValue()))
                            .withParameter(new PartStat(PartStat.NEEDS_ACTION.getValue()))
                            .withParameter(new Rsvp(true))
                            .withParameter(new XNoGuestsParameter())
                            .getFluentTarget())
                    .withProperty(
                        new Attendee(URI.create(MAILTO + client))
                            .withParameter(Role.REQ_PARTICIPANT)
                            .withParameter(new Cn(clientName))
                            .withParameter(new CuType(CuType.INDIVIDUAL.getValue()))
                            .withParameter(new Role(Role.REQ_PARTICIPANT.getValue()))
                            .withParameter(new PartStat(PartStat.NEEDS_ACTION.getValue()))
                            .withParameter(new Rsvp(true))
                            .withParameter(new XNoGuestsParameter())
                            .getFluentTarget())
                    .withProperty(new Description(eventName))
                    .withProperty(new Priority(Priority.VALUE_MEDIUM))
                    .withProperty(new Clazz(Clazz.VALUE_PUBLIC))
                    .withProperty(new Status(Status.VALUE_CONFIRMED))
                    .withProperty(new Sequence(0))
                    .withProperty(new Transp(Transp.VALUE_OPAQUE))
                    .getFluentTarget()
            )
            .getFluentTarget();
    }

    public static class XNoGuestsParameter extends Parameter {
        public XNoGuestsParameter() {
            super("X-NUM-GUESTS");
        }

        @Override
        public String getValue() {
            return "0";
        }
    }
}
