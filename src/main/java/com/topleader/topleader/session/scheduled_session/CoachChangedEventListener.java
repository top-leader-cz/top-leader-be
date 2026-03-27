package com.topleader.topleader.session.scheduled_session;

import com.topleader.topleader.common.event.CoachChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CoachChangedEventListener {

    private final ScheduledSessionService scheduledSessionService;

    @EventListener
    public void onCoachChanged(CoachChangedEvent event) {
        scheduledSessionService.deleteUserCoachedSessions(event.username());
    }
}
