package com.topleader.topleader.common.calendar.google;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
public class GoogleCalendarApiClientFactory {

    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String FREEBUSY_URL = "https://www.googleapis.com/calendar/v3/freeBusy";

    private final RestClient restClient;

    @Value("${google.client.client-id}")
    private String clientId;

    @Value("${google.client.client-secret}")
    private String clientSecret;

    public TokenResponse exchangeCode(String code, String redirectUri) {
        var params = new LinkedMultiValueMap<String, String>();
        params.add("code", code);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        return restClient.post()
                .uri(TOKEN_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(params)
                .retrieve()
                .body(TokenResponse.class);
    }

    public String refreshAccessToken(String refreshToken) {
        var params = new LinkedMultiValueMap<String, String>();
        params.add("refresh_token", refreshToken);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("grant_type", "refresh_token");

        var response = restClient.post()
                .uri(TOKEN_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(params)
                .retrieve()
                .body(TokenResponse.class);

        return response != null ? response.accessToken() : null;
    }

    public FreeBusyResponse queryFreeBusy(String refreshToken, LocalDateTime from, LocalDateTime to) {
        var accessToken = refreshAccessToken(refreshToken);

        var body = Map.of(
                "timeZone", "UTC",
                "timeMin", from.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                "timeMax", to.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                "items", List.of(Map.of("id", "primary"))
        );

        return restClient.post()
                .uri(FREEBUSY_URL)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(FreeBusyResponse.class);
    }

    public record TokenResponse(
            String access_token,
            String refresh_token,
            String token_type,
            Integer expires_in
    ) {
        public String accessToken() {
            return access_token;
        }

        public String refreshToken() {
            return refresh_token;
        }
    }

    public record FreeBusyResponse(Map<String, CalendarEntry> calendars) {
        public record CalendarEntry(List<BusySlot> busy, List<ErrorEntry> errors) {}
        public record BusySlot(String start, String end) {}
        public record ErrorEntry(String domain, String reason) {}
    }
}
