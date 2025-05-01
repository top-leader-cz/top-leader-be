package com.topleader.topleader.calendar.google;

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
public class GoogleSyncStateMarkingJob {

    private final CalendarSyncInfoRepository repository;

    private final GoogleCalendarService googleCalendarService;



    @Secured({"JOB"})
    @GetMapping("verify-sync-gcal")
    public void fetchTokens() {
        log.info("Verifying Google tokens");
        repository.findBySyncTypeAndStatus(CalendarSyncInfo.SyncType.GOOGLE)
                .forEach(info -> {
                    var active = googleCalendarService.verifyToken(info.getRefreshToken());
                    repository.save(info
                            .setStatus(active ? CalendarSyncInfo.Status.ERROR : CalendarSyncInfo.Status.OK)
                            .setLastSync(LocalDateTime.now()));
                });
    }
}
