package com.topleader.topleader.common.meeting.zoom;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class ZoomApiClient {

    private static final int MEETING_DURATION_MINUTES = 60;

    private final RestClient restClient;

    @Value("${zoom.api-url:https://api.zoom.us/v2}")
    private String apiUrl;

    @Value("${zoom.oauth.token-url:https://zoom.us/oauth/token}")
    private String tokenUrl;

    @Value("${zoom.client-id:}")
    private String clientId;

    @Value("${zoom.client-secret:}")
    private String clientSecret;

    public TokenResponse exchangeCode(String code, String redirectUri) {
        var params = new LinkedMultiValueMap<String, String>();
        params.add("code", code);
        params.add("grant_type", "authorization_code");
        params.add("redirect_uri", redirectUri);

        return restClient.post()
                .uri(tokenUrl)
                .headers(h -> h.setBasicAuth(clientId, clientSecret))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(params)
                .retrieve()
                .body(TokenResponse.class);
    }

    public TokenResponse refreshToken(String refreshToken) {
        var params = new LinkedMultiValueMap<String, String>();
        params.add("refresh_token", refreshToken);
        params.add("grant_type", "refresh_token");

        return restClient.post()
                .uri(tokenUrl)
                .headers(h -> h.setBasicAuth(clientId, clientSecret))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(params)
                .retrieve()
                .body(TokenResponse.class);
    }

    public MeetingResponse createMeeting(String accessToken, String topic, int durationMinutes) {
        var body = Map.of(
                "topic", topic,
                "type", 2,
                "duration", durationMinutes,
                "settings", Map.of(
                        "join_before_host", true,
                        "waiting_room", false
                )
        );

        return restClient.post()
                .uri(apiUrl + "/users/me/meetings")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(MeetingResponse.class);
    }

    public UserInfoResponse getUserInfo(String accessToken) {
        return restClient.get()
                .uri(apiUrl + "/users/me")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(UserInfoResponse.class);
    }

    public record TokenResponse(String access_token, String refresh_token, String token_type, Integer expires_in) {
        public String accessToken() { return access_token; }
        public String refreshToken() { return refresh_token; }
    }

    public record MeetingResponse(Long id, String join_url, String start_url, String topic) {
        public String joinUrl() { return join_url; }
    }

    public record UserInfoResponse(String email, String first_name, String last_name) {
    }
}
