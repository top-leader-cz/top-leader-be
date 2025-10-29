/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach;

import com.topleader.topleader.email.EmailTemplateService;
import com.topleader.topleader.exception.NotFoundException;
import com.topleader.topleader.scheduled_session.ScheduledSession;
import com.topleader.topleader.scheduled_session.ScheduledSessionService;
import com.topleader.topleader.user.User;
import com.topleader.topleader.user.UserRepository;
import com.topleader.topleader.util.common.CommonUtils;
import com.topleader.topleader.util.image.ImageUtil;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import static java.util.stream.Collectors.toMap;


/**
 * @author Daniel Slavik
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/latest/coach-info")
public class CoachController {

    private final CoachRepository coachRepository;

    private final CoachImageRepository coachImageRepository;

    private final ScheduledSessionService sessionService;

    private final UserRepository userRepository;

    private final EmailTemplateService emailTemplateService;

    @GetMapping
    @Secured("COACH")
    @Transactional
    public CoachDto getCoachInfo(@AuthenticationPrincipal UserDetails user) {
        return coachRepository.findById(user.getUsername())
            .map(CoachDto::from)
            .orElse(CoachDto.EMPTY);
    }

    @PostMapping
    @Secured("COACH")
    public CoachDto setCoachInfo(@AuthenticationPrincipal UserDetails user, @RequestBody @Valid CoachDto request) {
        var coach = coachRepository.findById(user.getUsername())
                .orElse(new Coach()
                        .setUsername(user.getUsername())
                        .setUser(userRepository.findById(user.getUsername())
                                .map(u -> u.setEmail(request.email))
                                .orElseThrow(CommonUtils.entityNotFound("user " + user.getUsername()))));

        return CoachDto.from(coachRepository.save(request.updateCoach(coach)));
    }

    @Transactional
    @Secured("COACH")
    @PostMapping("/photo")
    public void setCoachInfo(@AuthenticationPrincipal UserDetails user, @RequestParam("image") MultipartFile file) throws IOException {

        coachImageRepository.save(new CoachImage()
            .setUsername(user.getUsername())
            .setType(file.getContentType())
            .setImageData(ImageUtil.compressImage(file.getBytes()))
        );
    }

    @Secured("COACH")
    @GetMapping("/photo")
    public ResponseEntity<byte[]> getCoachPhoto(@AuthenticationPrincipal UserDetails user) {

        return coachImageRepository.findById(user.getUsername())
            .map(i -> ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.valueOf(i.getType()))
                .body(ImageUtil.decompressImage(i.getImageData()))
            )
            .orElseThrow(NotFoundException::new);
    }

    @Transactional
    @Secured("COACH")
    @GetMapping("/upcoming-sessions")
    public List<UpcomingSessionDto> getUpcomingSessions(@AuthenticationPrincipal UserDetails user) {

        final var sessions = sessionService.listCoachesFutureSessions(user.getUsername());

        if (sessions.isEmpty()) {
            return List.of();
        }

        final var clients = userRepository.findAllById(sessions.stream()
                .map(ScheduledSession::getUsername)
                .collect(Collectors.toSet())
            ).stream()
            .collect(toMap(User::getUsername, Function.identity()));

        return sessions.stream()
            .map(s -> UpcomingSessionDto.from(s, clients.get(s.getUsername())))
            .sorted(Comparator.comparing(UpcomingSessionDto::time))
            .toList();

    }

    @Transactional
    @DeleteMapping("/upcoming-sessions/{sessionId}")
    public void cancelSession(@PathVariable Long sessionId, @AuthenticationPrincipal UserDetails user) {

        if (sessionService.listCoachesFutureSessions(user.getUsername())
            .stream().noneMatch(s -> s.getId().equals(sessionId))
        ) {
            throw new NotFoundException();
        }

        emailTemplateService.sendCancelAlertEmail(sessionId);
        sessionService.cancelSession(sessionId);
    }

    public record UpcomingSessionDto(
        Long id,
        String username,
        String firstName,
        String lastName,
        LocalDateTime time
    ) {

        public static UpcomingSessionDto from(ScheduledSession s, User u) {
            return new UpcomingSessionDto(
                s.getId(),
                u.getUsername(),
                u.getFirstName(),
                u.getLastName(),
                s.getTime()
            );
        }
    }


    public record CoachDto(

        boolean publicProfile,
        @Size(max = 240)
        String firstName,
        @Size(max = 240)
        String lastName,

        @Size(max = 240)
        @Email
        String email,

        @Size(max = 1000)
        String webLink,

        @Size(max = 1000)
        String bio,

        @NotNull
        Set<String> languages,

        @NotNull
        Set<String> fields,

        LocalDate experienceSince,

        @NotNull
        Coach.CertificateType certificate,

        String timeZone,

        String linkedinProfile,

        @NotEmpty
        Set<Coach.PrimaryRole> primaryRoles
    ) {
        public static final CoachDto EMPTY = new CoachDto(false, null, null, null, null, null, Set.of(), Set.of(), null, null, null, null, Set.of(Coach.PrimaryRole.COACH));

        public static CoachDto from(Coach c) {
            var user = c.getUser();
            return new CoachDto(
                    c.isPublicProfile(),
                    user.getFirstName(),
                    user.getLastName(),
                    c.getUserEmail(),
                    c.getWebLink(),
                    c.getBio(),
                    c.getLanguages(),
                    c.getFields(),
                    c.getExperienceSince(),
                    c.getCertificate(),
                    user.getTimeZone(),
                    c.getLinkedinProfile(),
                    c.getPrimaryRoles()
            );
        }


        public Coach updateCoach(Coach coach) {
            return coach
                    .setPublicProfile(publicProfile)
                    .setWebLink(webLink)
                    .setBio(bio)
                    .setLanguages(languages)
                    .setFields(fields)
                    .setExperienceSince(experienceSince)
                    .setLinkedinProfile(linkedinProfile)
                    .setCertificate(certificate)
                    .setPrimaryRoles(primaryRoles)
                    .setUser(updateUser(coach.getUser()));
        }

        public User updateUser(User user) {
            return user.setFirstName(firstName())
                    .setEmail(email())
                    .setLastName(lastName())
                    .setTimeZone(timeZone())
                    ;
        }

    }

}
