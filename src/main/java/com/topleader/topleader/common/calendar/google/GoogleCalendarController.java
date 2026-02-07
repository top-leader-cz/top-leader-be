/*
 * Copyright (c) 2024 Price f(x), s.r.o.
 */
package com.topleader.topleader.common.calendar.google;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    private static final String AUTH_URL = "https://accounts.google.com/o/oauth2/auth";
    private static final String SCOPES = "openid https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/calendar.readonly";

    private final GoogleCalendarApiClientFactory clientFactory;

    private final GoogleCalendarService calendarService;

    @Value("${google.client.redirectUri}")
    private String redirectUri;

    @Value("${google.client.client-id}")
    private String clientId;

    @Value("${top-leader.app-url}")
    private String appUrl;


    @GetMapping("/login/google")
    public RedirectView googleConnectionStatus(
        @AuthenticationPrincipal UserDetails u
    ) {
        return new RedirectView(authorize(u.getUsername()));
    }

    @GetMapping(value = "/login/google", params = {"code", "state"})
    public ResponseEntity<String> oauth2Callback(
        @RequestParam(value = "code") String code,
        @RequestParam(value = "state") String state
    ) {
        var response = clientFactory.exchangeCode(code, redirectUri);

        var userEmail = new String(Base64.getDecoder().decode(state));

        calendarService.storeTokenInfo(userEmail, response);

        var redirectUrl = appUrl + "/#/sync-success?provider=gcal";
        var html = """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"><title>Redirecting...</title></head>
            <body>
            <script>window.location.href = "%s";</script>
            <noscript><a href="%s">Click here to continue</a></noscript>
            </body>
            </html>
            """.formatted(redirectUrl, redirectUrl);

        return ResponseEntity.ok()
            .contentType(MediaType.TEXT_HTML)
            .body(html);
    }

    private String authorize(String username) {
        var state = Base64.getEncoder().encodeToString(username.getBytes());
        return AUTH_URL
                + "?client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&response_type=code"
                + "&scope=" + URLEncoder.encode(SCOPES, StandardCharsets.UTF_8)
                + "&access_type=offline"
                + "&prompt=consent"
                + "&state=" + URLEncoder.encode(state, StandardCharsets.UTF_8);
    }
}
