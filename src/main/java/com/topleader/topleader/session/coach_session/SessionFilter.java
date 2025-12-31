package com.topleader.topleader.session.coach_session;


import com.topleader.topleader.session.scheduled_session.ScheduledSession;

import java.time.LocalDateTime;

public record SessionFilter(String client, ScheduledSession.Status status, LocalDateTime from, LocalDateTime to) {

}
