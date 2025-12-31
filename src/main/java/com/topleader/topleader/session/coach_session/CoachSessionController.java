package com.topleader.topleader.session.coach_session;


import com.topleader.topleader.session.scheduled_session.ScheduledSessionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
    public Page<CoachSessionView.CoachSessionViewDto> getSessions(@PageableDefault(size = 25, sort = "date") Pageable pageable,
                                                                  @RequestBody SessionFilter filter,
                                                                  @AuthenticationPrincipal UserDetails user) {
        return repository.findAll(SessionSpecification.withFilter(filter, user.getUsername()), pageable)
                .map(CoachSessionView::toDto);

    }

    @Secured({"COACH", "ADMIN"})
    @PatchMapping
    public SessionDto updateSession(@AuthenticationPrincipal UserDetails user, @RequestBody SessionDto session) {
        return scheduledSessionRepository.findByCoachUsernameAndId(user.getUsername(), session.id())
                .map(s -> s.setStatus(session.status()).setUpdatedAt(LocalDateTime.now()))
                .map(scheduledSessionRepository::save)
                .map(SessionDto::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Entity not found session"));
    }

    @Secured({"COACH", "ADMIN"})
    @GetMapping("clients")
    public List<Client> fetchClients(@AuthenticationPrincipal UserDetails user) {
        return repository.fetchClients(user.getUsername());
    }

}
