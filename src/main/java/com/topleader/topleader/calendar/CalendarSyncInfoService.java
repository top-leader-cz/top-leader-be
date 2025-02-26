package com.topleader.topleader.calendar;

import com.topleader.topleader.calendar.calendly.CalendlyProperties;
import com.topleader.topleader.calendar.calendly.domain.CalendlyInfo;
import com.topleader.topleader.calendar.calendly.domain.TokenResponse;
import com.topleader.topleader.calendar.domain.CalendarSyncInfo;
import jakarta.persistence.EntityNotFoundException;
import jnr.ffi.annotations.In;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.encodeBasicAuth;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarSyncInfoService {

    private final RestClient restClient;

    private final CalendlyProperties properties;

    private final CalendarSyncInfoRepository repository;

    public TokenResponse fetchTokens(CalendarSyncInfo info) {
        log.info("Refreshing Calendly token");
        var body = new LinkedMultiValueMap<String, String>();
        body.add("grant_type", "refresh_token");
        body.add("refresh_token", info.getRefreshToken());
        body.add("redirect_uri", properties.getRedirectUri());

        return restClient.post().uri(properties.getBaseAuthUrl().concat("/oauth/token"))
                .header(AUTHORIZATION, basic())
                .body(body)
                .retrieve()
                .body(TokenResponse.class);
    }

    public Optional<CalendarSyncInfo> getInfo(String username, CalendarSyncInfo.SyncType type) {
        return repository.findById(new CalendarSyncInfo.CalendarInfoId(username, type));
    }

    private String basic() {
        return "Basic " + encodeBasicAuth(properties.getClientId(), properties.getClientSecrets(), Charset.defaultCharset());
    }

    public CalendarSyncInfo save(CalendarSyncInfo info) {
        return repository.save(info);
    }
}
