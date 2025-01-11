package com.topleader.topleader.coach.list;

import com.topleader.topleader.calendar.google.GoogleCalendarService;
import com.topleader.topleader.coach.CoachRepository;
import com.topleader.topleader.coach.availability.CoachAvailabilityService;
import com.topleader.topleader.scheduled_session.ScheduledSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;


@Slf4j
@RestController
@RequestMapping("/api/protected/jobs")
@RequiredArgsConstructor
public class CoachAvailabilityJobController {

    private final CoachAvailabilityService coachAvailabilityService;

    private final CoachRepository coachRepository;

    private final CoachListViewRepository coachListViewRepository;

    private final ScheduledSessionService scheduledSessionService;


    @Secured({"JOB"})
    @GetMapping("/set-free-slots")
    public void updateCoachAvailability() {
        var from = LocalDateTime.now();
        var to = from.minusDays(7);

        coachListViewRepository.fetchActiveCoachesUsernames().forEach(username -> {
            var haScheduledEvents = !scheduledSessionService.listCoachesFutureSessions(username).isEmpty();
            var freeSlots = haScheduledEvents || !coachAvailabilityService.getSyncEvents(username, from, to, true).isEmpty();

            coachRepository.updateCoachSetFreeSlots(username, freeSlots);
        });

    }
}
