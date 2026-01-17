package com.topleader.topleader.common.calendar.calendly;


import com.topleader.topleader.common.calendar.domain.CalendarSyncInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public RedirectView cal(@RequestParam String code, @RequestParam String username) {
        CalendarSyncInfo info;
        try {
            var tokens = calendlyService.fetchTokens(code, username);
            info = new CalendarSyncInfo()
                    .setUsername(username)
                    .setSyncType(CalendarSyncInfo.SyncType.CALENDLY)
                    .setRefreshToken(tokens.getRefreshToken())
                    .setAccessToken(tokens.getAccessToken())
                    .setOwnerUrl(tokens.getOwner())
                    .setStatus(CalendarSyncInfo.Status.OK)
                    .setLastSync(LocalDateTime.now());
        } catch (Exception e) {
            log.error("Failed to sync Calendly", e);
            return new RedirectView("/#/sync-error?provider=calendly&error=sync.failed");
        }

        log.info("Saving Calendly info: {}", info.getUsername());

        try {
            calendlyService.saveInfo(info);
            return new RedirectView("/#/sync-success?provider=calendly");
        } catch (Exception e) {
            log.error("Failed to save Calendly info", e);
            return new RedirectView("/#/sync-error?provider=calendly&error=user.email.not.matched");
        }
    }
}
