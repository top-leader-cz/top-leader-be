/*
 * Copyright (c) 2024 Price f(x), s.r.o.
 */
package com.topleader.topleader.common.calendar.google;

import com.topleader.topleader.common.calendar.CalendarSyncInfoRepository;
import com.topleader.topleader.common.calendar.CalendarToErrorHandler;
import com.topleader.topleader.common.calendar.domain.CalendarSyncInfo;
import com.topleader.topleader.common.calendar.domain.SyncEvent;
import com.topleader.topleader.common.calendar.settings.AvailabilitySettingRepository;
import com.topleader.topleader.common.util.transaction.TransactionService;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


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

    public void storeTokenInfo(String username, GoogleCalendarApiClientFactory.TokenResponse tokenResponse) {
        log.info("Storing token info for user {}", username);

        transactionService.execute(() -> {
            calendarSyncInfoRepository.deleteByUsername(username);
            availabilitySettingRepository.deleteByCoach(username);

            var info = new CalendarSyncInfo()
                    .setUsername(username)
                    .setSyncType(CalendarSyncInfo.SyncType.GOOGLE)
                    .setStatus(CalendarSyncInfo.Status.OK)
                    .setRefreshToken(tokenResponse.refreshToken())
                    .setAccessToken(tokenResponse.accessToken())
                    .setLastSync(LocalDateTime.now());
            calendarSyncInfoRepository.save(info);
        });

        log.info("Token info stored for user {}", username);
    }

    public List<SyncEvent> getUserEvents(String username, LocalDateTime startDate, LocalDateTime endDate, Boolean testFreeBusy) {

        if (Boolean.TRUE.equals(testFreeBusy)) {
            return calendarSyncInfoRepository.findByUsernameAndSyncType(username, CalendarSyncInfo.SyncType.GOOGLE)
                    .map(info -> {
                        try {
                            var response = clientFactory.queryFreeBusy(info.getRefreshToken(), startDate, endDate);
                            return Optional.ofNullable(response.calendars())
                                    .map(calendars -> calendars.entrySet().stream()
                                            .peek(e -> Optional.ofNullable(e.getValue().errors()).orElse(List.of())
                                                    .forEach(err -> log.error("Error in free busy response for user {} and calendar {}: {}", username, e.getKey(), err)))
                                            .flatMap(e -> Optional.ofNullable(e.getValue().busy()).orElse(List.of()).stream()
                                                    .map(b -> new SyncEvent(username, parseDateTime(b.start()), parseDateTime(b.end())))
                                            )
                                            .toList()
                                    )
                                    .orElse(List.of());
                        } catch (Exception e) {
                            errorHandler.handleError(info, e);
                            return List.<SyncEvent>of();
                        }
                    })
                    .orElse(List.of());
        }

        return List.of();
    }

    private static LocalDateTime parseDateTime(String dateTime) {
        return OffsetDateTime.parse(dateTime).withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }
}
