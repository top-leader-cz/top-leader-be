/*
 * Copyright (c) 2024 Price f(x), s.r.o.
 */
package com.topleader.topleader.calendar.google;

import com.topleader.topleader.user.UserRepository;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.topleader.topleader.util.common.user.UserUtils.getUserTimeZoneId;


/**
 * @author Daniel Slavik
 */
@Slf4j
@RestController
@RequestMapping("/api/latest/google-info")
@RequiredArgsConstructor
public class GoogleTokenInfoController {


    private final GoogleCalendarService calendarService;

    private final UserRepository userRepository;


    @GetMapping
    public GoogleTokenInfoDto getGoogleTokenInfo(
        @AuthenticationPrincipal UserDetails user
    ) {

        final var userZoneId = getUserTimeZoneId(userRepository.findById(user.getUsername()));

        return calendarService.getSyncInfoForUser(user.getUsername())
            .map(tokenInfo -> new GoogleTokenInfoDto(
                true,
                tokenInfo.getStatus(),
                tokenInfo.getLastSync().atZone(ZoneOffset.UTC).withZoneSameInstant(userZoneId).toLocalDateTime()
            ))
            .orElse(GoogleTokenInfoDto.EMPTY);
    }

    public record GoogleTokenInfoDto(
        Boolean active,
        GoogleCalendarSyncInfo.Status status,
        LocalDateTime lastSync) {

        private static final GoogleTokenInfoDto EMPTY = new GoogleTokenInfoDto(false, null, null);
    }

}
