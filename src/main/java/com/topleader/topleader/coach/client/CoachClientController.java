/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach.client;

import com.topleader.topleader.exception.ApiValidationException;
import com.topleader.topleader.notification.Notification;
import com.topleader.topleader.notification.NotificationService;
import com.topleader.topleader.notification.context.CoachUnlinkedNotificationContext;
import com.topleader.topleader.user.InvitationService;
import com.topleader.topleader.user.User;
import com.topleader.topleader.user.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.topleader.topleader.exception.ErrorCodeConstants.EMAIL_USED;


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

    private final InvitationService invitationService;

    private final PasswordEncoder passwordEncoder;

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

    @Transactional
    @Secured("COACH")
    @PostMapping
    public CoachClientDto inviteUser(
        @AuthenticationPrincipal UserDetails user, @RequestBody UserInvitationRequestDto request
    ) {

        final var coach = userRepository.findById(user.getUsername()).orElseThrow();

        if (userRepository.findById(request.email()).isPresent()) {
            throw new ApiValidationException(EMAIL_USED, "email", request.email(), "Already used");
        }

        final var createdUser = userRepository.save(
            new User()
                .setUsername(request.email())
                .setPassword(passwordEncoder.encode(UUID.randomUUID().toString()))
                .setFirstName(request.firstName())
                .setLastName(request.lastName())
                .setCoach(coach.getUsername())
                .setFreeCoach(coach.getUsername())
                .setAuthorities(Set.of(User.Authority.USER))
                .setCredit(0)
                .setTimeZone(coach.getTimeZone())
                .setRequestedBy(user.getUsername())
                .setLocale(request.locale())
                .setStatus(Boolean.TRUE.equals(request.isTrial) ? User.Status.AUTHORIZED : User.Status.PENDING));

        if (Boolean.TRUE.equals(request.isTrial)) {
            invitationService.sendInvite(InvitationService.UserInvitationRequestDto.from(createdUser, request.locale()));
        }

        return CoachClientDto.from(createdUser);
    }

    public record UserInvitationRequestDto(@NotEmpty String email, @NotEmpty String firstName, @NotEmpty String lastName, Boolean isTrial, String locale) {
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

        public static CoachClientDto from(User u) {
            return new CoachClientDto(
                u.getUsername(),
                u.getFirstName(),
                u.getLastName(),
                null,
                null
            );
        }
    }
}
