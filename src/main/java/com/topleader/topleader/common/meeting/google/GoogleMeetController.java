package com.topleader.topleader.common.meeting.google;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

import com.topleader.topleader.common.calendar.google.GoogleCalendarApiClientFactory;
import com.topleader.topleader.common.email.Templating;
import com.topleader.topleader.common.meeting.MeetingService;
import com.topleader.topleader.common.meeting.domain.MeetingInfo;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    public Object initiateOAuth(
            @AuthenticationPrincipal UserDetails u,
            HttpSession session
    ) {
        if (u == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
        }
        var state = UUID.randomUUID().toString();
        session.setAttribute(OAUTH_STATE_ATTR, state);
        return new RedirectView(buildAuthUrl(state));
    }

    @GetMapping(value = "/login/google-meet", params = {"code", "state"})
    public ResponseEntity<String> oauthCallback(
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            @AuthenticationPrincipal UserDetails u,
            HttpSession session
    ) {
        if (u == null) {
            log.warn("OAuth callback with expired session");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Session expired. Please log in and try again.");
        }

        var expectedState = (String) session.getAttribute(OAUTH_STATE_ATTR);
        session.removeAttribute(OAUTH_STATE_ATTR);

        if (expectedState == null || !expectedState.equals(state)) {
            log.warn("Invalid OAuth state parameter for Google Meet, user {}", u.getUsername());
            return ResponseEntity.badRequest().body("Invalid OAuth state");
        }

        var tokenResponse = clientFactory.exchangeCode(code, redirectUri);
        var email = fetchGoogleEmail(tokenResponse.accessToken());

        meetingService.storeConnection(u.getUsername(), MeetingInfo.Provider.GOOGLE, tokenResponse.refreshToken(), tokenResponse.accessToken(), email);

        var html = templating.getMessage(
                Map.of("redirectUrl", appUrl + "/#/sync-success?provider=gmeet"),
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
