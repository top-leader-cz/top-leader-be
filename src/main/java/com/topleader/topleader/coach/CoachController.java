/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach;

import com.topleader.topleader.exception.NotFoundException;
import com.topleader.topleader.util.image.ImageUtil;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


/**
 * @author Daniel Slavik
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/latest/coach-info")
public class CoachController {

    private final CoachRepository coachRepository;

    private final CoachImageRepository coachImageRepository;

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
        String webLink,

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
                c.getWebLink(),
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
                .setWebLink(webLink)
                .setBio(bio)
                .setLanguages(languages)
                .setFields(fields)
                .setExperienceSince(experienceSince)
                .setRate(rate);
        }


    }

}
