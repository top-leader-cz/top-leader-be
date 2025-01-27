package com.topleader.topleader.calendar.calendly;


import com.topleader.topleader.calendar.domain.CalendarSyncInfo;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
public class CalendlyController {

    private final CalendlyService calendlyService;


    @GetMapping("/login/calendly")
    public RedirectView cal(String code) {
        CalendarSyncInfo info;
        try {
            var tokens = calendlyService.fetchTokens(code);
            var userInfo = calendlyService.getUserInfo(tokens);
             info = new CalendarSyncInfo()
                    .setId(new CalendarSyncInfo.CalendarInfoId(userInfo.getResource().getEmail(), CalendarSyncInfo.SyncType.CALENDLY))
                    .setRefreshToken(tokens.getRefreshToken())
                    .setAccessToken(tokens.getAccessToken())
                    .setOwnerUrl(tokens.getOwner())
                    .setStatus(CalendarSyncInfo.Status.OK)
                    .setLastSync(LocalDateTime.now());
        } catch (Exception e) {
            log.error("Failed to sync Calendly", e);
            return new RedirectView("/#/sync-error?provider=calendly&error=sync.failed");
        }

        log.info("Saving Calendly info: {}", info.getId().getUsername());

        return Try.run(() -> calendlyService.saveInfo(info))
                .map(d -> new RedirectView("/#/sync-success?provider=calendly"))
                .onFailure(e -> log.error("Failed to save Calendly info", e))
                .getOrElse(new RedirectView("/#/sync-error?provider=calendly&error=user.email.not.matched"));
    }
}
