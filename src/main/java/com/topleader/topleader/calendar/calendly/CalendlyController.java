package com.topleader.topleader.calendar.calendly;


import com.topleader.topleader.calendar.domain.CalendarSyncInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
public class CalendlyController {

    private final CalendlyService calendlyService;


    @GetMapping("/login/calendly")
    public RedirectView cal(String code) {
        var tokens = calendlyService.fetchTokens(code);
        var userInfo = calendlyService.getUserInfo(tokens);

        var info = new CalendarSyncInfo()
                .setId(new CalendarSyncInfo.CalendarInfoId(userInfo.getResource().getEmail(), CalendarSyncInfo.SyncType.CALENDLY))
//                .setUsername(userInfo.getResource().getEmail())
                .setRefreshToken(tokens.getRefreshToken())
                .setAccessToken(tokens.getAccessToken())
                .setOwnerUrl(tokens.getOwner())
//                .setSyncType(CalendarSyncInfo.SyncType.CALENDLY)
                .setStatus(CalendarSyncInfo.Status.OK);


        log.info("Saving Calendly info: {}", info);

        calendlyService.saveInfo(info);

        return new RedirectView("/#/sync-success?provider=calendly");

    }


}
