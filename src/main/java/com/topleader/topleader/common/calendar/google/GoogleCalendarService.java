/*
 * Copyright (c) 2024 Price f(x), s.r.o.
 */
package com.topleader.topleader.common.calendar.google;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.topleader.topleader.common.calendar.CalendarSyncInfoRepository;
import com.topleader.topleader.common.calendar.CalendarToErrorHandler;
import com.topleader.topleader.common.calendar.domain.CalendarSyncInfo;
import com.topleader.topleader.common.calendar.domain.SyncEvent;
import com.topleader.topleader.coach.availability.settings.AvailabilitySettingRepository;
import com.topleader.topleader.common.util.transaction.TransactionService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.topleader.topleader.common.util.common.DateUtils.convertToDateTime;


/**
 * @author Daniel Slavik
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleCalendarService {

    private final GoogleCalendarApiClientFactory clientFactory;

    private final CalendarSyncInfoRepository calendarSyncInfoRepository;


    private final AvailabilitySettingRepository availabilitySettingRepository;

    private final TransactionService transactionService;

    private final CalendarToErrorHandler errorHandler;

    @Transactional
    public void storeTokenInfo(String username, TokenResponse tokenResponse) {
        log.info("caStoring token info for user {} token info: {}", username, tokenResponse);

        var id = new CalendarSyncInfo.CalendarInfoId(username, CalendarSyncInfo.SyncType.GOOGLE);
        transactionService.execute(() -> {
            calendarSyncInfoRepository.deleteByUsername(username);
            availabilitySettingRepository.deleteByCoach(username);
            calendarSyncInfoRepository.flush();

            var info = new CalendarSyncInfo()
                    .setId(id)
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
}
