package com.topleader.topleader.common.meeting.zoom;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

import com.topleader.topleader.common.email.Templating;
import com.topleader.topleader.common.meeting.MeetingService;
import com.topleader.topleader.common.meeting.domain.MeetingInfo;
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

@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
public class ZoomController {

    private static final String AUTH_URL = "https://zoom.us/oauth/authorize";
    private static final String OAUTH_STATE_ATTR = "oauth_state_zoom";

    private final ZoomApiClient zoomApiClient;

    private final MeetingService meetingService;

    private final Templating templating;

    @Value("${zoom.client-id:}")
    private String clientId;

    @Value("${zoom.redirect-uri:http://localhost:8080/login/zoom}")
    private String redirectUri;

    @Value("${top-leader.app-url}")
    private String appUrl;

    @GetMapping("/login/zoom")
    public RedirectView initiateOAuth(
            @AuthenticationPrincipal UserDetails u,
            HttpSession session
    ) {
        var state = UUID.randomUUID().toString();
        session.setAttribute(OAUTH_STATE_ATTR, state);
        return new RedirectView(buildAuthUrl(state));
    }

    @GetMapping(value = "/login/zoom", params = "error")
    public ResponseEntity<String> oauthError(
            @RequestParam("error") String error,
            @AuthenticationPrincipal UserDetails u
    ) {
        log.warn("Zoom OAuth error for user {}: {}", u.getUsername(), error);
        return redirectToApp("/#/sync-error?provider=zoom&error=" + URLEncoder.encode(error, StandardCharsets.UTF_8));
    }

    @GetMapping(value = "/login/zoom", params = {"code", "state"})
    public ResponseEntity<String> oauthCallback(
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            @AuthenticationPrincipal UserDetails u,
            HttpSession session
    ) {
        var expectedState = (String) session.getAttribute(OAUTH_STATE_ATTR);
        session.removeAttribute(OAUTH_STATE_ATTR);

        if (expectedState == null || !expectedState.equals(state)) {
            log.warn("Invalid OAuth state parameter for Zoom, user {}", u.getUsername());
            return redirectToApp("/#/sync-error?provider=zoom&error=invalid_state");
        }

        try {
            var tokenResponse = zoomApiClient.exchangeCode(code, redirectUri);
            var email = fetchZoomEmail(tokenResponse.accessToken());
            meetingService.storeConnection(u.getUsername(), MeetingInfo.Provider.ZOOM, tokenResponse.refreshToken(), tokenResponse.accessToken(), email);
            return redirectToApp("/#/sync-success?provider=zoom");
        } catch (Exception e) {
            log.error("Zoom token exchange failed for user {}", u.getUsername(), e);
            return redirectToApp("/#/sync-error?provider=zoom&error=token_exchange_failed");
        }
    }

    private ResponseEntity<String> redirectToApp(String path) {
        var html = templating.getMessage(
                Map.of("redirectUrl", appUrl + path),
                "templates/oauth/redirect.html");
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }

    private String fetchZoomEmail(String accessToken) {
        try {
            var response = zoomApiClient.getUserInfo(accessToken);
            return response != null ? response.email() : null;
        } catch (Exception e) {
            log.warn("Failed to fetch Zoom user email", e);
            return null;
        }
    }

    private String buildAuthUrl(String state) {
        return AUTH_URL
                + "?client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&response_type=code"
                + "&state=" + URLEncoder.encode(state, StandardCharsets.UTF_8);
    }
}
