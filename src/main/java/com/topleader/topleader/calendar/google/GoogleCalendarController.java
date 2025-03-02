/*
 * Copyright (c) 2024 Price f(x), s.r.o.
 */
package com.topleader.topleader.calendar.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.oauth2.Oauth2Scopes;
import java.io.IOException;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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

    @Value("${google.client.redirectUri}")
    private String redirectURI;


    @GetMapping("/login/google")
    public RedirectView googleConnectionStatus(
        @AuthenticationPrincipal UserDetails u
    ) {
        return new RedirectView(authorize(u.getUsername()));
    }

    @GetMapping(value = "/login/google", params = {"code", "state"})
    public RedirectView oauth2Callback(
        @RequestParam(value = "code") String code,
        @RequestParam(value = "state") String state
        ) throws IOException {

        final var response = flow.newTokenRequest(code).setRedirectUri(redirectURI).execute();

        final var userEmail = new String(Base64.decodeBase64(state));

        calendarService.storeTokenInfo(userEmail, response);

        return new RedirectView("/#/sync-success?provider=gcal");
    }

    private String authorize(String username) {
        return flow.newAuthorizationUrl()
            .setRedirectUri(redirectURI)
            .setApprovalPrompt("force")
            .setAccessType("offline")
            .setScopes(Set.of(
                Oauth2Scopes.OPENID,
                Oauth2Scopes.USERINFO_EMAIL,
                CalendarScopes.CALENDAR_READONLY
            ))
            .setState(Base64.encodeBase64String(username.getBytes()))
            .build();
    }
}
