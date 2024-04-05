/*
 * Copyright (c) 2024 Price f(x), s.r.o.
 */
package com.topleader.topleader.google;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.topleader.topleader.util.transaction.TransactionService;
import io.vavr.Tuple2;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.StreamSupport;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import static com.topleader.topleader.util.common.DateUtils.convertToDateTime;
import static java.util.Objects.isNull;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toMap;


/**
 * @author Daniel Slavik
 */
@Slf4j
@Service
@AllArgsConstructor
public class GoogleCalendarService {

    private final GoogleCalendarApiClientFactory clientFactory;

    private final SyncEventRepository syncEventRepository;

    private final GoogleCalendarSyncInfoRepository calendarSyncInfoRepository;

    private final TransactionService transactionService;

    @Async
    public void startInitCalendarSynchronize(String username, TokenResponse tokenResponse) {
        log.info("Starting initial event synchronization for user {}", username);

        transactionService.execute(() -> {
            final var info = calendarSyncInfoRepository.findById(username)
                .orElseGet(() -> new GoogleCalendarSyncInfo()
                    .setUsername(username)
                );
            info
                .setStatus(GoogleCalendarSyncInfo.Status.IN_PROGRESS)
                .setLastSync(LocalDateTime.now())
                .setRefreshToken(tokenResponse.getRefreshToken())
                .setSyncToken(null);
            calendarSyncInfoRepository.save(info);
            syncEventRepository.deleteAllByUsername(username);
        });

        performFullSync(username, clientFactory.prepareCalendarClient(tokenResponse));

        log.info("Finishing initial event synchronization for user {}", username);
    }

    @Async
    public void startIncrementalSync() {
        final var users = transactionService.execute(() ->
            calendarSyncInfoRepository.findAll().stream()
                .filter(info -> info.getStatus() == GoogleCalendarSyncInfo.Status.OK)
                .map(info -> new Tuple2<>(info.getUsername(), isNull(info.getSyncToken())))
                .toList()
        );

        users.forEach(user -> {
            if (Boolean.TRUE.equals(user._2())) {
                startFullCalendarSync(user._1());
            } else {
                startIncrementalCalendarSync(user._1());
            }
        });
    }

    public void startFullCalendarSync(String username) {
        log.info("Starting full event synchronization for user {}", username);

        final var dbInfo = markAsStarted(username);

        performFullSync(username, clientFactory.prepareCalendarClient(dbInfo.getRefreshToken()));

        log.info("Finishing initial event synchronization for user {}", username);
    }


    private void performFullSync(String username, Calendar client) {
        coverErrorCaseFor(username, () -> {
            final var iterator = new EventIterator(client, "primary");

            final var events = StreamSupport.stream(
                    Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
                    false)
                .filter(not(GoogleCalendarService::isCanceled))
                .map(event -> new SyncEvent()
                    .setUsername(username)
                    .setExternalId(event.getId())
                    .setStartDate(convertToDateTime(event.getStart()))
                    .setEndDate(convertToDateTime(event.getEnd()))
                )
                .toList();


            transactionService.execute(() -> {
                final var info = calendarSyncInfoRepository.findById(username)
                    .orElseGet(() -> new GoogleCalendarSyncInfo()
                        .setUsername(username)
                    );
                info
                    .setStatus(GoogleCalendarSyncInfo.Status.OK)
                    .setLastSync(LocalDateTime.now())
                    .setSyncToken(iterator.getNextSyncToken())
                    .setEnforceFullSync(LocalDateTime.now().plusMonths(1))
                ;
                calendarSyncInfoRepository.save(info);
                syncEventRepository.deleteAllByUsername(username);
                if (!events.isEmpty()) {
                    syncEventRepository.saveAll(events);
                }
            });
        });
    }

    private void startIncrementalCalendarSync(String username) {
        log.info("Starting incremental event synchronization for user {}", username);

        final var dbInfo = markAsStarted(username);

        coverErrorCaseFor(username, () -> {
            final var client = clientFactory.prepareCalendarClient(dbInfo.getRefreshToken());

            final var iterator = new EventIterator(client, "primary", dbInfo.getSyncToken());

            final var events = StreamSupport.stream(
                    Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
                    false)
                .toList();

            final var toDelete = events.stream()
                .filter(GoogleCalendarService::isCanceled)
                .map(Event::getId)
                .toList();

            transactionService.execute(() -> {

                if (!toDelete.isEmpty()) {
                    syncEventRepository.deleteAllByUsernameAndExternalIdIn(username, toDelete);
                }

                final var existingEvents = syncEventRepository.findAllByUsername(username).stream()
                    .collect(toMap(SyncEvent::getExternalId, Function.identity()));

                final var toUpdate = events.stream()
                    .filter(e -> existingEvents.containsKey(e.getId()))
                    .map(e -> existingEvents.get(e.getId())
                        .setStartDate(convertToDateTime(e.getStart()))
                        .setEndDate(convertToDateTime(e.getEnd()))
                    )
                    .toList();

                final var toInsert = events.stream()
                    .filter(e -> !existingEvents.containsKey(e.getId()))
                    .filter(not(GoogleCalendarService::isCanceled))
                    .map(e -> new SyncEvent()
                        .setUsername(username)
                        .setExternalId(e.getId())
                        .setStartDate(convertToDateTime(e.getStart()))
                        .setEndDate(convertToDateTime(e.getEnd())))
                    .toList();

                if (!toUpdate.isEmpty()) {
                    syncEventRepository.saveAll(toUpdate);
                }
                if (!toInsert.isEmpty()) {
                    syncEventRepository.saveAll(toInsert);
                }

                calendarSyncInfoRepository.findById(username)
                    .ifPresent(info -> {
                        info
                            .setStatus(GoogleCalendarSyncInfo.Status.OK)
                            .setLastSync(LocalDateTime.now())
                            .setSyncToken(info.getEnforceFullSync().isBefore(LocalDateTime.now()) ? null : iterator.getNextSyncToken());
                        calendarSyncInfoRepository.save(info);
                    });
            });
        });

        log.info("Finishing incremental event synchronization for user {}", username);

    }

    private GoogleCalendarSyncInfo markAsStarted(String username) {
        return transactionService.execute(() -> {
            final var info = calendarSyncInfoRepository.findById(username).orElseThrow();

            info
                .setStatus(GoogleCalendarSyncInfo.Status.IN_PROGRESS)
                .setLastSync(LocalDateTime.now());
            calendarSyncInfoRepository.save(info);
            return info;
        });
    }

    private void coverErrorCaseFor(String username, SyncProcessor runnable) {
        try {
            runnable.process();
        } catch (Exception e) {
            log.error("Unable to synchronize events", e);

            transactionService.execute(() -> {
                final var info = calendarSyncInfoRepository.findById(username)
                    .orElseGet(() -> new GoogleCalendarSyncInfo()
                        .setUsername(username)
                    );
                info
                    .setStatus(GoogleCalendarSyncInfo.Status.ERROR)
                    .setLastSync(LocalDateTime.now());
                calendarSyncInfoRepository.save(info);
            });
        }
    }

    private static boolean isCanceled(Event event) {
        return "cancelled".equalsIgnoreCase(event.getStatus());
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
                                .map(b -> new SyncEvent()
                                    .setUsername(username)
                                    .setStartDate(convertToDateTime(b.getStart()))
                                    .setEndDate(convertToDateTime(b.getEnd()))
                                )
                            )
                            .toList();
                    } catch (IOException e) {
                        log.error("Unable to fetch free busy info for user " + username, e);
                        return List.<SyncEvent>of();
                    }

                })
                .orElse(List.of());
        }

        return syncEventRepository.findAllByUsernameAndStartDateAndEndDateBetween(username, startDate, endDate);
    }

    @FunctionalInterface
    private interface SyncProcessor {
        void process() throws GeneralSecurityException, IOException;
    }
}
