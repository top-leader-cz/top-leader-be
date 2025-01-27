package com.topleader.topleader.calendar.calendly;


import com.topleader.topleader.calendar.CalendarSyncInfoService;
import com.topleader.topleader.calendar.calendly.domain.CalendarTokenInfo;
import com.topleader.topleader.calendar.domain.CalendarSyncInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;


@Slf4j
@RestController
@RequestMapping("/api/latest/calendly-info")
@RequiredArgsConstructor
public class CalendlyTokenInfoController {

    private final CalendarSyncInfoService calendarService;

    @GetMapping
    public CalendarTokenInfo getGoogleTokenInfo(@AuthenticationPrincipal UserDetails user) {
        return calendarService.getInfo(user.getUsername(), CalendarSyncInfo.SyncType.CALENDLY)
                .map(tokenInfo -> new CalendarTokenInfo(
                        true,
                        tokenInfo.getStatus(),
                        tokenInfo.getLastSync().atZone(ZoneId.systemDefault())
                ))
                .orElse(CalendarTokenInfo.EMPTY);
    }

}
