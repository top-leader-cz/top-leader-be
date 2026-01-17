package com.topleader.topleader.session.coach_session;


import com.topleader.topleader.common.exception.NotFoundException;
import com.topleader.topleader.session.scheduled_session.ScheduledSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/latest/coach-sessions")
@RequiredArgsConstructor
public class CoachSessionController {

    private final ScheduledSessionRepository scheduledSessionRepository;

    private final CoachSessionViewRepository repository;

    @Secured({"COACH", "ADMIN"})
    @PostMapping
    public List<CoachSessionView.CoachSessionViewDto> getSessions(@RequestBody SessionFilter filter,
                                                                  @AuthenticationPrincipal UserDetails user) {
        var statusStr = filter.status() != null ? filter.status().name() : null;
        return repository.findFiltered(user.getUsername(), filter.client(), statusStr, filter.from(), filter.to())
                .stream()
                .map(CoachSessionView::toDto)
                .toList();
    }

    @Secured({"COACH", "ADMIN"})
    @PatchMapping
    public SessionDto updateSession(@AuthenticationPrincipal UserDetails user, @RequestBody SessionDto session) {
        return scheduledSessionRepository.findByCoachUsernameAndId(user.getUsername(), session.id())
                .map(s -> s.setStatus(session.status()).setUpdatedAt(LocalDateTime.now()))
                .map(scheduledSessionRepository::save)
                .map(SessionDto::toDto)
                .orElseThrow(NotFoundException::new);
    }

    @Secured({"COACH", "ADMIN"})
    @GetMapping("clients")
    public List<Client> fetchClients(@AuthenticationPrincipal UserDetails user) {
        return repository.fetchClients(user.getUsername());
    }

}
