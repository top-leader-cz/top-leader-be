/*
 * Copyright (c) 2024 Price f(x), s.r.o.
 */
package com.topleader.topleader.common.calendar.google;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

import com.topleader.topleader.common.email.Templating;
import org.springframework.web.util.HtmlUtils;
import jakarta.servlet.http.HttpSession;
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

    private final Templating templating;

    @Value("${google.client.redirectUri}")
    private String redirectUri;

    @Value("${google.client.client-id}")
    private String clientId;

    @Value("${top-leader.app-url}")
    private String appUrl;


    private static final String OAUTH_STATE_ATTR = "oauth_state";

    @GetMapping("/login/google")
    public RedirectView googleConnectionStatus(
        @AuthenticationPrincipal UserDetails u,
        HttpSession session
    ) {
        var state = UUID.randomUUID().toString();
        session.setAttribute(OAUTH_STATE_ATTR, state);
        return new RedirectView(authorize(state));
    }

    @GetMapping(value = "/login/google", params = "error")
    public ResponseEntity<String> oauth2Error(
        @RequestParam("error") String error,
        @AuthenticationPrincipal UserDetails u
    ) {
        log.warn("Google Calendar OAuth error for user {}: {}", u.getUsername(), error);
        return redirectToApp("/#/sync-error?provider=google-calendar&error=" + URLEncoder.encode(error, StandardCharsets.UTF_8));
    }

    @GetMapping(value = "/login/google", params = {"code", "state"})
    public ResponseEntity<String> oauth2Callback(
        @RequestParam(value = "code") String code,
        @RequestParam(value = "state") String state,
        @AuthenticationPrincipal UserDetails u,
        HttpSession session
    ) {
        var expectedState = (String) session.getAttribute(OAUTH_STATE_ATTR);
        session.removeAttribute(OAUTH_STATE_ATTR);

        if (expectedState == null || !expectedState.equals(state)) {
            log.warn("Invalid OAuth state parameter for user {}", u.getUsername());
            return redirectToApp("/#/sync-error?provider=google-calendar&error=invalid_state");
        }

        try {
            var response = clientFactory.exchangeCode(code, redirectUri);
            calendarService.storeTokenInfo(u.getUsername(), response);
            return redirectToApp("/#/sync-success?provider=google-calendar");
        } catch (Exception e) {
            log.error("Google Calendar token exchange failed for user {}", u.getUsername(), e);
            return redirectToApp("/#/sync-error?provider=google-calendar&error=token_exchange_failed");
        }
    }

    private ResponseEntity<String> redirectToApp(String path) {
        var url = HtmlUtils.htmlEscape(appUrl + path);
        var html = templating.getMessage(
                Map.of("redirectUrl", url),
                "templates/oauth/redirect.html");
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }

    private String authorize(String state) {
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
