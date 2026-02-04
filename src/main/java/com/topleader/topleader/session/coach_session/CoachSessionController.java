package com.topleader.topleader.session.coach_session;


import com.topleader.topleader.session.scheduled_session.ScheduledSessionRepository;
import com.topleader.topleader.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/latest/coach-sessions")
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class CoachSessionController {

    private final ScheduledSessionRepository scheduledSessionRepository;

    private final CoachSessionViewRepository repository;

    @Secured({"COACH", "ADMIN"})
    @PostMapping
    public Page<CoachSessionView.CoachSessionViewDto> getSessions(@PageableDefault(size = 25, sort = "date") Pageable pageable,
                                                                  @RequestBody SessionFilter filter,
                                                                  @AuthenticationPrincipal UserDetails user) {
        var statusStr = Optional.ofNullable(filter.status()).map(Enum::name).orElse(null);
        var content = repository.findFiltered(user.getUsername(), filter.client(), statusStr, filter.from(), filter.to(), pageable)
                .stream()
                .map(CoachSessionView::toDto)
                .toList();
        var total = repository.countFiltered(user.getUsername(), filter.client(), statusStr, filter.from(), filter.to());
        return new PageImpl<>(content, pageable, total);
    }

    @Secured({"COACH", "ADMIN"})
    @PatchMapping
    public SessionDto updateSession(@AuthenticationPrincipal UserDetails user, @RequestBody SessionDto session) {
        log.info("Updating session: id={}, newStatus={}, coachUsername={}", session.id(), session.status(), user.getUsername());
        var existingSession = scheduledSessionRepository.findByCoachUsernameAndId(user.getUsername(), session.id());
        return existingSession
                .map(s -> s.setStatus(session.status()).setUpdatedAt(LocalDateTime.now()))
                .map(scheduledSessionRepository::save)
                .map(saved -> {
                    log.info("Saved session: id={}, status={}", saved.getId(), saved.getStatus());
                    return SessionDto.toDto(saved);
                })
                .orElseThrow(NotFoundException::new);
    }

    @Secured({"COACH", "ADMIN"})
    @GetMapping("clients")
    public List<Client> fetchClients(@AuthenticationPrincipal UserDetails user) {
        return repository.findByCoachUsername(user.getUsername())
                .stream()
                .map(view -> new Client(view.getClient(), view.getFirstName(), view.getLastName()))
                .distinct()
                .toList();
    }

}
