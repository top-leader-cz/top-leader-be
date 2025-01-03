package com.topleader.topleader.calendar.calendly;


import com.topleader.topleader.calendar.calendly.domain.CalendlyInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
public class CalendlyController {

    private final CalendlyService calendlyService;


    @GetMapping("/login/calendly")
    public void cal(String code) {
        var tokens = calendlyService.fetchTokens(code);
        var userInfo = calendlyService.getUserInfo(tokens);

        calendlyService.saveInfo(new CalendlyInfo()
                .setUsername(userInfo.getResource().getEmail())
                .setRefreshToken(tokens.getRefreshToken())
                .setOwnerUrl(tokens.getOwner()));

//        List<SyncEvent> userEvents = calendlyService.getUserEvents(userInfo.getResource().getEmail(), LocalDateTime.now(), LocalDateTime.now().plusDays(5));
    }




}
