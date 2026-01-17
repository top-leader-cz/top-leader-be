package com.topleader.topleader.common.calendar.calendly;


import com.topleader.topleader.common.calendar.CalendarSyncInfoService;
import com.topleader.topleader.common.calendar.CalendarToErrorHandler;
import com.topleader.topleader.common.calendar.calendly.domain.CalendarTokenInfo;
import com.topleader.topleader.common.calendar.domain.CalendarSyncInfo;
import com.topleader.topleader.common.util.common.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;


@Slf4j
@RestController
@RequestMapping("/api/latest")
@RequiredArgsConstructor
public class CalendlyTokenInfoController {

    private final CalendarSyncInfoService calendarService;

    private final CalendlyService calendlyService;
    private final CalendarToErrorHandler errorHandler;

    @GetMapping("calendly-info")
    public CalendarTokenInfo getGoogleTokenInfo(@AuthenticationPrincipal UserDetails user) {
        return calendarService.getInfo(user.getUsername(), CalendarSyncInfo.SyncType.CALENDLY)
                .map(info -> CommonUtils.tryMapOrElse(
                        () -> calendlyService.refreshTokens(info),
                        tokens -> calendarService.save(info.setRefreshToken(tokens.getRefreshToken())
                                .setAccessToken(tokens.getAccessToken())
                                .setLastSync(LocalDateTime.now())),
                        info,
                        e -> errorHandler.handleError(info, e)))
                .map(tokenInfo -> new CalendarTokenInfo(
                        true,
                        tokenInfo.getStatus(),
                        tokenInfo.getLastSync().atZone(ZoneId.systemDefault())
                ))
                .orElse(CalendarTokenInfo.EMPTY);
    }

    @DeleteMapping("calendly-disconnect")
    public void disconnect(@AuthenticationPrincipal UserDetails user) {
        calendarService.disconnect(user.getUsername(), CalendarSyncInfo.SyncType.CALENDLY);
    }

}
