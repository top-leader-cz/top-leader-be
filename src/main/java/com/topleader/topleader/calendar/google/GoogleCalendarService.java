/*
 * Copyright (c) 2024 Price f(x), s.r.o.
 */
package com.topleader.topleader.calendar.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.topleader.topleader.calendar.CalendarSyncInfoRepository;
import com.topleader.topleader.calendar.CalendarToErrorHandler;
import com.topleader.topleader.calendar.domain.CalendarSyncInfo;
import com.topleader.topleader.calendar.domain.SyncEvent;
import com.topleader.topleader.util.transaction.TransactionService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import io.vavr.control.Try;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import static com.topleader.topleader.util.common.DateUtils.convertToDateTime;


/**
 * @author Daniel Slavik
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleCalendarService {

    private final GoogleCalendarApiClientFactory clientFactory;

    private final CalendarSyncInfoRepository calendarSyncInfoRepository;

    private final TransactionService transactionService;

    private final RestClient restClient;

    private final CalendarToErrorHandler errorHandler;

    private final UserDetailService userDetailService;

    @Value("${google.client.client-id}")
    private String clientId;

    @Value("${google.client.client-secret}")
    private String clientSecret;

    public void storeTokenInfo(String username, TokenResponse tokenResponse) {
        log.info("Storing token info for user {} token info: {}", username, tokenResponse);

        var id = new CalendarSyncInfo.CalendarInfoId(username, CalendarSyncInfo.SyncType.GOOGLE);
        transactionService.execute(() -> {
            final var info = calendarSyncInfoRepository.findById(id).orElse(new CalendarSyncInfo().setId(id));
            info
                    .setStatus(CalendarSyncInfo.Status.OK)
                    .setRefreshToken(tokenResponse.getRefreshToken())
                    .setAccessToken(tokenResponse.getAccessToken())
                    .setLastSync(LocalDateTime.now());

            calendarSyncInfoRepository.save(info);
        });

        log.info("Storing token info for user {}", username);
    }

    public List<SyncEvent> getUserEvents(String username, LocalDateTime startDate, LocalDateTime endDate, Boolean testFreeBusy) {

        if (Boolean.TRUE.equals(testFreeBusy)) {
            return calendarSyncInfoRepository.findById(new CalendarSyncInfo.CalendarInfoId(username, CalendarSyncInfo.SyncType.GOOGLE))
                    .map(info -> {
                        try {
                            return clientFactory.prepareCalendarClient(info.getRefreshToken())
                                    .freebusy()
                                    .query(new com.google.api.services.calendar.model.FreeBusyRequest()
                                            .setTimeZone("UTC")
                                            .setTimeMin(convertToDateTime(startDate))
                                            .setTimeMax(convertToDateTime(endDate))
                                            .setItems(List.of(new com.google.api.services.calendar.model.FreeBusyRequestItem()
                                                    .setId("primary")
                                            ))
                                    )
                                    .execute()
                                    .getCalendars()
                                    .entrySet().stream()
                                    .peek(e -> Optional.ofNullable(e.getValue().getErrors()).orElse(List.of())
                                            .forEach(err -> log.error("Error in free busy response for user " + username + " and calendar " + e.getKey() + ": {}", err)))
                                    .flatMap(e -> e.getValue().getBusy().stream()
                                            .map(b -> new SyncEvent(username, convertToDateTime(b.getStart()), convertToDateTime(b.getEnd())))
                                    )
                                    .toList();
                        } catch (IOException e) {
                            errorHandler.handleError(info, e);
                            return List.<SyncEvent>of();
                        }

                    })
                    .orElse(List.of());
        }

        return List.of();
    }

    public boolean verifyToken(String refreshToken) {
        return Try.of(() ->
                        restClient.post().uri("https://oauth2.googleapis.com/token")
                                .body(new VerifyRequest().setClientId(clientId).setClientSecret(clientSecret).setRefreshToken(refreshToken))
                                .retrieve()
                                .body(VerifyResponse.class)
                                .getAccessToken() != null
                ).onFailure(e -> log.warn("Error verifying token", e))
                .getOrElse(false);
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VerifyResponse {
        @JsonProperty("access_token")
        private String accessToken;

    }

    @Data
    @Accessors(chain = true)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VerifyRequest {

        @JsonProperty("client_id")
        private String clientId;

        @JsonProperty("client_secret")
        private String clientSecret;

        @JsonProperty("refresh_token")
        private String refreshToken;

        @JsonProperty("grant_type")
        private String grantType = "refresh_token";


    }
}
