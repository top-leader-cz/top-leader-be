package com.topleader.topleader.coach.session;

import com.topleader.topleader.scheduled_session.ScheduledSession;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public record SessionDto(long id, ZonedDateTime date, String client, ScheduledSession.Status status) {

    public static SessionDto toDto(ScheduledSession session) {
        return new SessionDto(session.getId(), session.getTime().atZone(ZoneId.of("UTC")), session.getUsername(), session.getStatus());
    }


}
