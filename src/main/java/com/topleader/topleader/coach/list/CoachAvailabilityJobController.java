package com.topleader.topleader.coach.list;

import com.topleader.topleader.calendar.google.GoogleCalendarService;
import com.topleader.topleader.coach.CoachRepository;
import com.topleader.topleader.coach.availability.CoachAvailabilityService;
import com.topleader.topleader.scheduled_session.ScheduledSessionService;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Slf4j
@RestController
@RequestMapping("/api/protected/jobs")
@RequiredArgsConstructor
public class CoachAvailabilityJobController {

    private final CoachAvailabilityService coachAvailabilityService;

    private final CoachRepository coachRepository;

    private final CoachListViewRepository coachListViewRepository;



    @Secured({"JOB"})
    @GetMapping("/set-free-slots")
    public void updateCoachAvailability() {
        log.info("CoachAvailabilityJobController START");
        var from = LocalDate.now().atStartOfDay();
        var to = from.plusDays(7);

        coachListViewRepository.fetchActiveCoachesUsernames().forEach(username -> {
            Try.run(() -> {

                        var freeSlots = !coachAvailabilityService.getAvailabilitySplitIntoHoursFiltered(username, from, to, true).isEmpty();

                        log.info("setting freeSlot: {}, for coach: {}", freeSlots, username);
                        coachRepository.updateCoachSetFreeSlots(username, freeSlots);
                    })
                    .onFailure(e -> log.error("Error updating coach availability for coach: {}", username, e));
        });
        log.info("CoachAvailabilityJobController FINISH");
    }
}
