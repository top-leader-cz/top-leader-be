/*
 * Copyright (c) 2024 Price f(x), s.r.o.
 */
package com.topleader.topleader.calendar.google;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.topleader.topleader.calendar.SyncEvent;
import com.topleader.topleader.util.transaction.TransactionService;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.topleader.topleader.util.common.DateUtils.convertToDateTime;


/**
 * @author Daniel Slavik
 */
@Slf4j
@Service
@AllArgsConstructor
public class GoogleCalendarService {

    private final GoogleCalendarApiClientFactory clientFactory;

    private final GoogleCalendarSyncInfoRepository calendarSyncInfoRepository;

    private final TransactionService transactionService;

    public void storeTokenInfo(String username, TokenResponse tokenResponse) {
        log.info("Storing token info for user {} token info: {}", username, tokenResponse);

        transactionService.execute(() -> {
            final var info = calendarSyncInfoRepository.findById(username)
                .orElseGet(() -> new GoogleCalendarSyncInfo()
                    .setUsername(username)
                );
            info
                .setStatus(GoogleCalendarSyncInfo.Status.OK)
                .setLastSync(LocalDateTime.now())
                .setRefreshToken(tokenResponse.getRefreshToken())
                .setSyncToken(null);
            calendarSyncInfoRepository.save(info);
        });

        log.info("Storing token info for user {}", username);
    }

    public List<SyncEvent> getUserEvents(String username, LocalDateTime startDate, LocalDateTime endDate, Boolean testFreeBusy) {

        if (Boolean.TRUE.equals(testFreeBusy)) {
            return calendarSyncInfoRepository.findById(username)
                .map(GoogleCalendarSyncInfo::getRefreshToken)
                .map(token -> {
                    try {
                        return clientFactory.prepareCalendarClient(token)
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
                        log.error("Unable to fetch free busy info for user " + username, e);
                        return List.<SyncEvent>of();
                    }

                })
                .orElse(List.of());
        }

        return List.of();
    }

    public Optional<GoogleCalendarSyncInfo> getSyncInfoForUser(String username) {
        return calendarSyncInfoRepository.findById(username);
    }

}
