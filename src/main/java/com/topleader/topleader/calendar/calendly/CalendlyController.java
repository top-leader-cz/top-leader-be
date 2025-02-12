package com.topleader.topleader.calendar.calendly;


import com.topleader.topleader.calendar.calendly.domain.CalendlyUserInfo;
import com.topleader.topleader.calendar.calendly.domain.TokenResponse;
import com.topleader.topleader.calendar.domain.CalendarSyncInfo;
import com.topleader.topleader.util.common.CommonUtils;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
        CalendlyUserInfo info;
        TokenResponse tokens;
        try {
            tokens = calendlyService.fetchTokens(code);
            info = calendlyService.getUserInfo(tokens);
        } catch (Exception e) {
            log.error("Failed to sync Calendly", e);
            return new RedirectView("/#/sync-error?provider=calendly&error=sync.failed");
        }

        return Try.run(() -> {
                    var email = info.getResource().getEmail();
                    log.info("Saving Calendly info: {}", email);
                    var calendarInfo = calendlyService.findInfo(email).orElseThrow(CommonUtils.entityNotFound("User not found " + email));

                    calendarInfo.setRefreshToken(tokens.getRefreshToken())
                            .setAccessToken(tokens.getAccessToken())
                            .setOwnerUrl(tokens.getOwner())
                            .setStatus(CalendarSyncInfo.Status.OK)
                            .setLastSync(LocalDateTime.now());
                    calendlyService.saveInfo(calendarInfo);
                })
                .map(d -> new RedirectView("/#/sync-success?provider=calendly"))
                .onFailure(e -> log.error("Failed to save Calendly info", e))
                .getOrElse(new RedirectView("/#/sync-error?provider=calendly&error=user.email.not.matched"));
    }
}
