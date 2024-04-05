/*
 * Copyright (c) 2024 Price f(x), s.r.o.
 */
package com.topleader.topleader.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.oauth2.Oauth2Scopes;
import java.io.IOException;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;


/**
 * @author Daniel Slavik
 */
@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
public class GoogleCalendarController {


    private final GoogleAuthorizationCodeFlow flow;

    private final GoogleCalendarService calendarService;

    private final GoogleCalendarApiClientFactory googleCalendarApiClientFactory;

    @Value("${google.client.redirectUri}")
    private String redirectURI;


    @GetMapping("/login/google")
    public RedirectView googleConnectionStatus() {
        return new RedirectView(authorize());
    }

    @GetMapping(value = "/login/google", params = "code")
    public RedirectView oauth2Callback(@RequestParam(value = "code") String code) throws IOException {

        final var response = flow.newTokenRequest(code).setRedirectUri(redirectURI).execute();

        final var userinfo = googleCalendarApiClientFactory.prepareOauthClient(response)
            .userinfo()
            .v2()
            .me()
            .get()
            .execute();

        calendarService.startInitCalendarSynchronize(userinfo.getEmail(), response);

        return new RedirectView("/sync-success");
    }

    private String authorize() {
        return flow.newAuthorizationUrl()
            .setRedirectUri(redirectURI)
            .setAccessType("offline")
            .setScopes(Set.of(
                Oauth2Scopes.OPENID,
                Oauth2Scopes.USERINFO_EMAIL,
                CalendarScopes.CALENDAR_EVENTS_READONLY,
                CalendarScopes.CALENDAR_READONLY
            ))
            .build();
    }
}
