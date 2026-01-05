/*
 * Copyright (c) 2024 Price f(x), s.r.o.
 */
package com.topleader.topleader.common.calendar.google;

import com.topleader.topleader.common.calendar.CalendarSyncInfoService;
import com.topleader.topleader.common.calendar.calendly.domain.CalendarTokenInfo;
import com.topleader.topleader.common.calendar.domain.CalendarSyncInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * @author Daniel Slavik
 */
@Slf4j
@RestController
@RequestMapping("/api/latest")
@RequiredArgsConstructor
public class GoogleTokenInfoController {


    private final CalendarSyncInfoService calendarService;

    private final GoogleCalendarService googleCalendarService;


    @GetMapping("google-info")
    public CalendarTokenInfo getGoogleTokenInfo(@AuthenticationPrincipal UserDetails user) {

        return calendarService.getInfo(user.getUsername(), CalendarSyncInfo.SyncType.GOOGLE)
                .map(info -> {
                       var active = googleCalendarService.verifyToken(info.getRefreshToken());
                       info.setStatus(active ? CalendarSyncInfo.Status.OK : CalendarSyncInfo.Status.ERROR);
                       return calendarService.save(info);
                })
                .map(tokenInfo -> new CalendarTokenInfo(
                        true,
                        tokenInfo.getStatus(),
                        tokenInfo.getLastSync().atZone(ZoneId.of("UTC")))
                )
                .orElse(CalendarTokenInfo.EMPTY);
    }

    @DeleteMapping("google-disconnect")
    public void disconnect(@AuthenticationPrincipal UserDetails user) {
        calendarService.disconnect(user.getUsername(), CalendarSyncInfo.SyncType.GOOGLE);
    }
}
