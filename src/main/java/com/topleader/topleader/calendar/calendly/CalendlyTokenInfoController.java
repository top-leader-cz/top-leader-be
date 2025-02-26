package com.topleader.topleader.calendar.calendly;


import com.topleader.topleader.calendar.CalendarSyncInfoRepository;
import com.topleader.topleader.calendar.CalendarSyncInfoService;
import com.topleader.topleader.calendar.CalendarToErrorHandler;
import com.topleader.topleader.calendar.calendly.domain.CalendarTokenInfo;
import com.topleader.topleader.calendar.domain.CalendarSyncInfo;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneId;


@Slf4j
@RestController
@RequestMapping("/api/latest/calendly-info")
@RequiredArgsConstructor
public class CalendlyTokenInfoController {

    private final CalendarSyncInfoService calendarService;
    private final CalendarToErrorHandler errorHandler;

    @GetMapping
    public CalendarTokenInfo getGoogleTokenInfo(@AuthenticationPrincipal UserDetails user) {
        return calendarService.getInfo(user.getUsername(), CalendarSyncInfo.SyncType.CALENDLY)
                .map(info ->
                     Try.of(() -> calendarService.fetchTokens(info))
                            .map(tokens ->
                                    calendarService.save(info.setRefreshToken(tokens.getRefreshToken())
                                            .setAccessToken(tokens.getAccessToken())
                                            .setLastSync(LocalDateTime.now()))
                            )
                            .onFailure(e -> errorHandler.handleError(info, e))
                            .getOrElse(info)
                )
                .map(tokenInfo -> new CalendarTokenInfo(
                        true,
                        tokenInfo.getStatus(),
                        tokenInfo.getLastSync().atZone(ZoneId.systemDefault())
                ))
                .orElse(CalendarTokenInfo.EMPTY);
    }

}
