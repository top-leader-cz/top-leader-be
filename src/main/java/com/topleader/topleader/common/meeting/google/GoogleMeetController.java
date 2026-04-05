package com.topleader.topleader.common.meeting.google;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

import com.topleader.topleader.common.calendar.google.GoogleCalendarApiClientFactory;
import org.springframework.web.util.HtmlUtils;
import com.topleader.topleader.common.email.Templating;
import com.topleader.topleader.common.meeting.MeetingService;
import com.topleader.topleader.common.meeting.domain.MeetingInfo;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.view.RedirectView;

@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
@Secured({"COACH"})
public class GoogleMeetController {

    private static final String AUTH_URL = "https://accounts.google.com/o/oauth2/auth";
    private static final String USERINFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";
    private static final String SCOPES = "openid https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/calendar.events";
    private static final String OAUTH_STATE_ATTR = "oauth_state_meet";

    private final GoogleCalendarApiClientFactory clientFactory;

    private final MeetingService meetingService;

    private final RestClient restClient;

    private final Templating templating;

    @Value("${google.client.meetRedirectUri}")
    private String redirectUri;

    @Value("${google.client.client-id}")
    private String clientId;

    @Value("${top-leader.app-url}")
    private String appUrl;

    @GetMapping("/login/google-meet")
    public RedirectView initiateOAuth(
            @AuthenticationPrincipal UserDetails u,
            HttpSession session
    ) {
        var state = UUID.randomUUID().toString();
        session.setAttribute(OAUTH_STATE_ATTR, state);
        return new RedirectView(buildAuthUrl(state));
    }

    @GetMapping(value = "/login/google-meet", params = "error")
    public ResponseEntity<String> oauthError(
            @RequestParam("error") String error,
            @AuthenticationPrincipal UserDetails u
    ) {
        log.warn("Google Meet OAuth error for user {}: {}", u.getUsername(), error);
        return redirectToApp("/#/sync-error?provider=gmeet&error=" + URLEncoder.encode(error, StandardCharsets.UTF_8));
    }

    @GetMapping(value = "/login/google-meet", params = {"code", "state"})
    public ResponseEntity<String> oauthCallback(
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            @AuthenticationPrincipal UserDetails u,
            HttpSession session
    ) {
        var expectedState = (String) session.getAttribute(OAUTH_STATE_ATTR);
        session.removeAttribute(OAUTH_STATE_ATTR);

        if (expectedState == null || !expectedState.equals(state)) {
            log.warn("Invalid OAuth state parameter for Google Meet, user {}", u.getUsername());
            return redirectToApp("/#/sync-error?provider=gmeet&error=invalid_state");
        }

        try {
            var tokenResponse = clientFactory.exchangeCode(code, redirectUri);
            var email = fetchGoogleEmail(tokenResponse.accessToken());
            meetingService.storeConnection(u.getUsername(), MeetingInfo.Provider.GOOGLE, tokenResponse.refreshToken(), tokenResponse.accessToken(), email);
            return redirectToApp("/#/sync-success?provider=gmeet");
        } catch (Exception e) {
            log.error("Google Meet token exchange failed for user {}", u.getUsername(), e);
            return redirectToApp("/#/sync-error?provider=gmeet&error=token_exchange_failed");
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

    private String fetchGoogleEmail(String accessToken) {
        try {
            var response = restClient.get()
                    .uri(USERINFO_URL)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(UserInfoResponse.class);
            return response != null ? response.email() : null;
        } catch (Exception e) {
            log.warn("Failed to fetch Google user email", e);
            return null;
        }
    }

    private String buildAuthUrl(String state) {
        return AUTH_URL
                + "?client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&response_type=code"
                + "&scope=" + URLEncoder.encode(SCOPES, StandardCharsets.UTF_8)
                + "&access_type=offline"
                + "&prompt=consent"
                + "&state=" + URLEncoder.encode(state, StandardCharsets.UTF_8);
    }

    private record UserInfoResponse(String email, String name, Map<String, Object> other) {
    }
}
