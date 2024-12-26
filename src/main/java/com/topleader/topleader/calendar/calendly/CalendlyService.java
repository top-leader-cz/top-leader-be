package com.topleader.topleader.calendar.calendly;

import com.fasterxml.jackson.databind.JsonNode;
import com.topleader.topleader.calendar.SyncEvent;
import com.topleader.topleader.calendar.calendly.domain.CalendlyInfo;
import com.topleader.topleader.calendar.calendly.domain.CalendlyUserInfo;
import com.topleader.topleader.calendar.calendly.domain.TokenResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


import static com.topleader.topleader.util.common.user.UserUtils.getUserCalendlyUuid;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.encodeBasicAuth;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalendlyService {

    private final CalendlyInfoRepository calendlyInfoRepository;


    private final CalendlyProperties properties;

    private final RestClient restClient;

    public void saveInfo(CalendlyInfo info) {
        log.info("Saving Calendly info: {}", info.getUsername());
        calendlyInfoRepository.save(info);
    }

    public TokenResponse fetchTokens(String authorizationCode) {
        log.info("Fetching Calendly tokens");
        var body = new LinkedMultiValueMap<String, String>();
        body.add("grant_type", "authorization_code");
        body.add("code", authorizationCode);
        body.add("redirect_uri", properties.getRedirectUri());

        return restClient.post().uri(properties.getBaseAuthUrl().concat("/oauth/token"))
                .header(AUTHORIZATION, basic())
                .body(body)
                .retrieve()
                .body(TokenResponse.class);
    }

    public CalendlyUserInfo getUserInfo(TokenResponse tokens) {
        log.info("Fetching Calendly user info: {}", tokens.getOwner());

        return restClient.get().uri(properties.getBaseApiUrl().concat("/users/" + getUserCalendlyUuid(tokens.getOwner())))
                .header(AUTHORIZATION, bearer(tokens.getAccessToken()))
                .retrieve()
                .body(CalendlyUserInfo.class);
    }

    public List<SyncEvent> getUserEvents(String username, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Looking for Calendly info");
        return  calendlyInfoRepository.findById(username)
                .map(info -> {
                    log.info("Fetching Calendly user events");
                    var response = restClient.get().uri(properties.getBaseApiUrl().concat("/scheduled_events?user={user}&start_time={start_time}&end_time={end_time}"),
                                    info.getOwnerUrl(),
                                    toCalendlyFormat(startDate),
                                    toCalendlyFormat(endDate))
                            .header(AUTHORIZATION, bearer(getAccessToken(info)))
                            .retrieve()
                            .body(JsonNode.class);

                    var events = new ArrayList<SyncEvent>();
                    response.get("collection").forEach(node ->
                            events.add(new SyncEvent(username, fromCalendlyFormat(node.get("start_time").asText()), fromCalendlyFormat(node.get("end_time").asText())))
                    );

                    return events;
                })
                .orElse(new ArrayList<>());
    }

    public String getAccessToken(CalendlyInfo info) {
        log.info("Refreshing Calendly token");
        var body = new LinkedMultiValueMap<String, String>();
        body.add("grant_type", "refresh_token");
        body.add("refresh_token", info.getRefreshToken());
        body.add("redirect_uri", properties.getRedirectUri());

        var tokens = restClient.post().uri(properties.getBaseAuthUrl().concat("/oauth/token"))
                .header(AUTHORIZATION, basic())
                .body(body)
                .retrieve()
                .body(TokenResponse.class);

        calendlyInfoRepository.save(info.setRefreshToken(tokens.getRefreshToken()));

        return tokens.getAccessToken();
    }

    private String basic() {
        return "Basic " + encodeBasicAuth(properties.getClientId(), properties.getClientSecrets(), Charset.defaultCharset());
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    public static String toCalendlyFormat(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.BASIC_ISO_DATE);
    }

    public static LocalDateTime fromCalendlyFormat(String dateTime) {
        return LocalDateTime.parse(dateTime, DateTimeFormatter.ISO_ZONED_DATE_TIME);
    }
}
