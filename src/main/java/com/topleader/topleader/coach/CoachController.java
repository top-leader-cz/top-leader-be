/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach;

import com.topleader.topleader.common.email.EmailTemplateService;
import com.topleader.topleader.common.exception.NotFoundException;
import com.topleader.topleader.session.scheduled_session.ScheduledSession;
import com.topleader.topleader.session.scheduled_session.ScheduledSessionService;
import com.topleader.topleader.user.User;
import com.topleader.topleader.user.UserRepository;
import com.topleader.topleader.common.util.common.CommonUtils;
import com.topleader.topleader.common.util.image.ImageUtil;
import org.springframework.transaction.annotation.Transactional;
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
    public CoachDto getCoachInfo(@AuthenticationPrincipal UserDetails user) {
        var userEntity = userRepository.findByUsername(user.getUsername()).orElseThrow();
        return coachRepository.findByUsername(user.getUsername())
            .map(c -> CoachDto.from(c, userEntity))
            .orElse(CoachDto.EMPTY);
    }

    @PostMapping
    @Secured("COACH")
    @Transactional
    public CoachDto setCoachInfo(@AuthenticationPrincipal UserDetails user, @RequestBody @Valid CoachDto request) {
        var userEntity = userRepository.findByUsername(user.getUsername())
                .map(u -> u.setEmail(request.email))
                .orElseThrow(CommonUtils.entityNotFound("user " + user.getUsername()));
        userRepository.save(request.updateUser(userEntity));

        var coach = coachRepository.findByUsername(user.getUsername())
                .orElse(new Coach().setUsername(user.getUsername()));

        return CoachDto.from(coachRepository.save(request.updateCoach(coach)), userEntity);
    }

    @Secured("COACH")
    @PostMapping("/photo")
    public void setCoachInfo(@AuthenticationPrincipal UserDetails user, @RequestParam("image") MultipartFile file) throws IOException {

        var image = coachImageRepository.findByUsername(user.getUsername())
                .orElse(new CoachImage().setUsername(user.getUsername()));
        image.setType(file.getContentType())
            .setImageData(ImageUtil.compressImage(file.getBytes()));
        coachImageRepository.save(image);
    }

    @Secured("COACH")
    @GetMapping("/photo")
    public ResponseEntity<byte[]> getCoachPhoto(@AuthenticationPrincipal UserDetails user) {

        return coachImageRepository.findByUsername(user.getUsername())
            .map(i -> ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.valueOf(i.getType()))
                .body(ImageUtil.decompressImage(i.getImageData()))
            )
            .orElseThrow(NotFoundException::new);
    }

    @Secured("COACH")
    @GetMapping("/upcoming-sessions")
    public List<UpcomingSessionDto> getUpcomingSessions(@AuthenticationPrincipal UserDetails user) {

        final var sessions = sessionService.listCoachesFutureSessions(user.getUsername());

        if (sessions.isEmpty()) {
            return List.of();
        }

        final var clients = userRepository.findAllByUsernameIn(sessions.stream()
                        .map(ScheduledSession::getUsername)
                        .collect(Collectors.toSet())).stream()
            .collect(toMap(User::getUsername, Function.identity()));

        return sessions.stream()
            .map(s -> UpcomingSessionDto.from(s, clients.get(s.getUsername())))
            .sorted(Comparator.comparing(UpcomingSessionDto::time))
            .toList();

    }

    @DeleteMapping("/upcoming-sessions/{sessionId}")
    public void cancelSession(@PathVariable Long sessionId, @AuthenticationPrincipal UserDetails user) {

        if (sessionService.listCoachesFutureSessions(user.getUsername())
            .stream().noneMatch(s -> s.getId().equals(sessionId))
        ) {
            throw new NotFoundException();
        }

        emailTemplateService.sendCancelAlertEmail(sessionId);
        sessionService.cancelSessionByCoach(sessionId, user.getUsername());
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
        List<String> languages,

        @NotNull
        List<String> fields,

        LocalDate experienceSince,

        String rate,

        Integer rateOrder,

        Integer internalRate,

        String linkedinProfile,

        boolean freeSlots,

        int priority,

        @NotEmpty
        Set<Coach.PrimaryRole> primaryRoles,

        Set<String> certificate,

        Set<String> baseLocations,

        String travelWillingness,

        Set<String> deliveryFormat,

        Set<String> serviceType,

        Set<String> topics,

        Set<String> diagnosticTools,

        Set<String> industryExperience,

        String references,

        String timeZone
    ) {
        public static final CoachDto EMPTY = new CoachDto(
            false, null, null, null, null, null, List.of(), List.of(), null, null, null, null, null, false, 0,
            Set.of(Coach.PrimaryRole.COACH), Set.of(), Set.of(), null, Set.of(), Set.of(), Set.of(), Set.of(), Set.of(), null, null
        );

        public static CoachDto from(Coach c, User user) {
            return new CoachDto(
                    c.isPublicProfile(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEmail(),
                    c.getWebLink(),
                    c.getBio(),
                    c.getLanguagesList(),
                    c.getFieldsList(),
                    c.getExperienceSince(),
                    c.getRate(),
                    c.getRateOrder(),
                    c.getInternalRate(),
                    c.getLinkedinProfile(),
                    c.isFreeSlots(),
                    c.getPriority(),
                    c.getPrimaryRolesSet() != null ? c.getPrimaryRolesSet() : Set.of(),
                    c.getCertificateSet() != null ? c.getCertificateSet() : Set.of(),
                    c.getBaseLocationsSet() != null ? c.getBaseLocationsSet() : Set.of(),
                    c.getTravelWillingness(),
                    c.getDeliveryFormatSet() != null ? c.getDeliveryFormatSet() : Set.of(),
                    c.getServiceTypeSet() != null ? c.getServiceTypeSet() : Set.of(),
                    c.getTopicsSet() != null ? c.getTopicsSet() : Set.of(),
                    c.getDiagnosticToolsSet() != null ? c.getDiagnosticToolsSet() : Set.of(),
                    c.getIndustryExperienceSet() != null ? c.getIndustryExperienceSet() : Set.of(),
                    c.getReferences(),
                    user.getTimeZone()
            );
        }


        public Coach updateCoach(Coach coach) {
            return coach
                    .setPublicProfile(publicProfile)
                    .setWebLink(webLink)
                    .setBio(bio)
                    .setLanguagesList(languages)
                    .setFieldsList(fields)
                    .setExperienceSince(experienceSince)
                    .setRate(rate)
                    .setRateOrder(rateOrder)
                    .setInternalRate(internalRate)
                    .setLinkedinProfile(linkedinProfile)
                    .setFreeSlots(freeSlots)
                    .setPriority(priority)
                    .setPrimaryRolesSet(primaryRoles)
                    .setCertificateSet(certificate)
                    .setBaseLocationsSet(baseLocations)
                    .setTravelWillingness(travelWillingness)
                    .setDeliveryFormatSet(deliveryFormat)
                    .setServiceTypeSet(serviceType)
                    .setTopicsSet(topics)
                    .setDiagnosticToolsSet(diagnosticTools)
                    .setIndustryExperienceSet(industryExperience)
                    .setReferences(references);
        }

        public User updateUser(User user) {
            return user.setFirstName(firstName())
                    .setEmail(email())
                    .setLastName(lastName())
                    .setTimeZone(timeZone());
        }

    }

}
