package com.topleader.topleader.calendar.calendly;


import com.topleader.topleader.calendar.CalendarSyncInfoRepository;
import com.topleader.topleader.calendar.CalendarSyncInfoService;
import com.topleader.topleader.calendar.CalendarToErrorHandler;
import com.topleader.topleader.calendar.domain.CalendarSyncInfo;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/protected/jobs")
@RequiredArgsConstructor
public class CalendlyRefreshAccessTokenJob {

    private final CalendarSyncInfoRepository repository;

    private final CalendarSyncInfoService calendarSyncInfoService;

    private final CalendarToErrorHandler errorHandler;


    @Secured({"JOB"})
    @GetMapping("refresh-tokens-calendly")
    public void fetchTokens() {
        log.info("Fetching Calendly tokens");
        repository.findBySyncType(CalendarSyncInfo.SyncType.CALENDLY)
                .forEach(info -> {
                    log.info("Refreshing tokens for user: {}", info.getUsername());
                    Try.of(() -> calendarSyncInfoService.fetchTokens(info))
                            .map(tokens -> repository.save(info.setRefreshToken(tokens.getRefreshToken())
                                            .setAccessToken(tokens.getAccessToken()))
                                    .setLastSync(LocalDateTime.now()))
                            .onFailure(e -> errorHandler.handleError(info, e));
                });
    }


}
