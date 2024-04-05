/*
 * Copyright (c) 2024 Price f(x), s.r.o.
 */
package com.topleader.topleader.google;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Iterator;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import static com.topleader.topleader.util.common.DateUtils.toDate;
import static java.util.Collections.emptyIterator;


/**
 * @author Daniel Slavik
 */
@Slf4j
public class EventIterator implements Iterator<Event> {

    private final Calendar.Events.List request;

    private Iterator<Event> iterator;

    private String pageToken;

    @Getter
    private String nextSyncToken;

    public EventIterator(Calendar service, String calendarId, String syncToken) throws IOException {
        this.request = service.events()
            .list(calendarId)
            .setSingleEvents(true)
            .setTimeZone("UTC")
            .setSyncToken(syncToken)
        ;
        this.iterator = null;
        this.pageToken = null;
        fetchNextPage();
    }

    public EventIterator(Calendar service, String calendarId) throws IOException {
        this.request = service.events()
            .list(calendarId)
            .setTimeMin(new DateTime(new Date()))
            .setTimeMax(new DateTime(toDate(LocalDateTime.now().plusMonths(2))))
            .setSingleEvents(true)
            .setTimeZone("UTC")
        ;
        this.iterator = null;
        this.pageToken = null;
        fetchNextPage();
    }

    @Override
    public boolean hasNext() {
        if (iterator == null || !iterator.hasNext()) {
            if (pageToken == null) {
                log.debug("No more pages to fetch");
                return false;
            }
            try {
                fetchNextPage();
            } catch (IOException e) {
                log.error("Unable to fetch next page", e);
                return false;
            }
        }
        return iterator != null && iterator.hasNext();
    }

    @Override
    public Event next() {
        if (!hasNext()) {
            throw new IllegalStateException("No more elements in the iterator");
        }
        return iterator.next();
    }

    private void fetchNextPage() throws IOException {
        Optional.ofNullable(pageToken).ifPresent(request::setPageToken);
        try {
            final var events = request.execute();
            final var items = events.getItems();
            this.iterator = items.iterator();
            this.pageToken = events.getNextPageToken();
            this.nextSyncToken = events.getNextSyncToken();
        } catch (GoogleJsonResponseException e) {
            if (e.getStatusCode() == 410) {
                // A 410 status code, "Gone", indicates that the sync token is invalid.
                log.info("Invalid sync token, clearing event store and re-syncing.");
                iterator = emptyIterator();
                pageToken = null;
                nextSyncToken = null;
            } else {
                throw e;
            }
        }
    }
}
