/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author Daniel Slavik
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/latest/coach-info")
public class CoachController {

    private final CoachRepository coachRepository;

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
    @Transactional
    public CoachDto setCoachInfo(@AuthenticationPrincipal UserDetails user, @RequestBody @Valid CoachDto request) {
        return CoachDto.from(coachRepository.save(request.toCoach(user.getUsername())));
    }

    public record CoachDto(
        @NotNull
        Boolean publicProfile,
        @Size(max = 240)
        String firstName,
        @Size(max = 240)
        String lastName,

        @Size(max = 240)
        @Email
        String email,

        @Size(max = 1000)
        byte[] photo,

        @Size(max = 1000)
        String bio,

        @NotNull
        Set<String> languages,

        @NotNull
        Set<String> fields,

        LocalDate experienceSince,

        @NotNull
        String rate
    ) {
        public static final CoachDto EMPTY = new CoachDto(false, null, null, null, null, null, Set.of(), Set.of(), null, null);

        public static CoachDto from(Coach c) {
            return new CoachDto(
                c.getPublicProfile(),
                c.getFirstName(),
                c.getLastName(),
                c.getEmail(),
                c.getPhoto(),
                c.getBio(),
                c.getLanguages(),
                c.getFields(),
                c.getExperienceSince(),
                c.getRate()
            );
        }

        public Coach toCoach(String username) {
            return new Coach()
                .setUsername(username)
                .setPublicProfile(publicProfile)
                .setFirstName(firstName)
                .setLastName(lastName)
                .setEmail(email)
                .setPhoto(photo)
                .setBio(bio)
                .setLanguages(languages)
                .setFields(fields)
                .setExperienceSince(experienceSince)
                .setRate(rate);
        }


    }

}
