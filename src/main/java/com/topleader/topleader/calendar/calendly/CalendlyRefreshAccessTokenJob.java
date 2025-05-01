package com.topleader.topleader.calendar.calendly;


import com.topleader.topleader.calendar.CalendarSyncInfoRepository;
import com.topleader.topleader.calendar.CalendarSyncInfoService;
import com.topleader.topleader.calendar.CalendarToErrorHandler;
import com.topleader.topleader.calendar.domain.CalendarSyncInfo;
import com.topleader.topleader.util.common.CommonUtils;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/api/protected/jobs")
@RequiredArgsConstructor
public class CalendlyRefreshAccessTokenJob {

    private final CalendarSyncInfoRepository repository;

    private final CalendlyService calendlyService;

    private final CalendarToErrorHandler errorHandler;


    @Secured({"JOB"})
    @GetMapping("refresh-tokens-calendly")
    public void fetchTokens() {
        log.info("Fetching Calendly tokens");
        repository.findBySyncTypeAndStatus(CalendarSyncInfo.SyncType.CALENDLY)
                .forEach(info -> {
                    CommonUtils.sleep(TimeUnit.MILLISECONDS, 100);
                    log.info("fetching tokens for user: {}", info.getUsername());
                    Try.of(() -> calendlyService.refreshTokens(info))
                            .map(tokens ->
                                 repository.save(info.setRefreshToken(tokens.getRefreshToken())
                                        .setAccessToken(tokens.getAccessToken())
                                        .setLastSync(LocalDateTime.now()))
                            )
                            .onFailure(e -> errorHandler.handleError(info, e));
                });
    }


}
