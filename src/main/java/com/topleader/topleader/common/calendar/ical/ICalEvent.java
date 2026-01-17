package com.topleader.topleader.common.calendar.ical;

/**
 * Simple wrapper for iCalendar event content.
 * Replaces iCal4j Calendar object with plain text generation.
 */
public record ICalEvent(String content) {

    @Override
    public String toString() {
        return content;
    }
}
