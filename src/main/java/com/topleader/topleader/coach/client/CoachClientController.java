/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach.client;

import com.topleader.topleader.notification.Notification;
import com.topleader.topleader.notification.NotificationService;
import com.topleader.topleader.notification.context.CoachUnlinkedNotificationContext;
import com.topleader.topleader.user.UserRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author Daniel Slavik
 */
@Slf4j
@RestController
@RequestMapping("/api/latest/coach-clients")
@AllArgsConstructor
public class CoachClientController {

    private final UserRepository userRepository;

    private final NotificationService notificationService;

    private final CoachClientViewRepository coachClientViewRepository;

    @Secured("COACH")
    @GetMapping
    public List<CoachClientDto> getClients(@AuthenticationPrincipal UserDetails user) {

        return coachClientViewRepository.findAllByCoach(user.getUsername()).stream()
            .map(CoachClientDto::from)
            .toList();
    }

    @Transactional
    @Secured("COACH")
    @DeleteMapping("/{username}")
    public void removeClient(@AuthenticationPrincipal UserDetails user, @PathVariable String username) {
        userRepository.findById(username)
            .filter(u -> user.getUsername().equals(u.getCoach()))
            .map(u -> u.setCoach(null))
            .ifPresent(u -> {
                userRepository.save(u);
                notificationService.addNotification(
                    new NotificationService.CreateNotificationRequest(
                        u.getUsername(),
                        Notification.Type.COACH_UNLINKED,
                        new CoachUnlinkedNotificationContext().setCoach(user.getUsername())
                    )
                );
            });
    }

    public record CoachClientDto(
        String username,
        String firstName,
        String lastName,
        LocalDateTime lastSession,
        LocalDateTime nextSession
    ) {

        public static CoachClientDto from(CoachClientView v) {
            return new CoachClientDto(
                v.getClient(),
                v.getClientFirstName(),
                v.getClientLastName(),
                v.getLastSession(),
                v.getNextSession()
            );
        }
    }
}
