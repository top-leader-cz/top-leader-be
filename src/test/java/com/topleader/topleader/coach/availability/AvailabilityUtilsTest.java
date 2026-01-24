package com.topleader.topleader.coach.availability;

import com.topleader.topleader.TestUtils;

import com.topleader.topleader.common.calendar.domain.AvailabilityUtils;
import com.topleader.topleader.common.calendar.domain.ReoccurringEventDto;
import com.topleader.topleader.common.calendar.domain.ReoccurringEventTimeDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;


class AvailabilityUtilsTest {

    @Test
    public void test() {
        var data = TestUtils.readFileAsJson("availability/calendly-availability.json");
        Assertions.assertThat(AvailabilityUtils.toReoccurringEvent(data)).
                containsExactly(new ReoccurringEventDto(
                        new ReoccurringEventTimeDto(DayOfWeek.MONDAY, LocalTime.of(8, 0)),
                        new ReoccurringEventTimeDto(DayOfWeek.MONDAY, LocalTime.of(11, 0))),
                        new ReoccurringEventDto(
                                new ReoccurringEventTimeDto(DayOfWeek.MONDAY, LocalTime.of(14, 0)),
                                new ReoccurringEventTimeDto(DayOfWeek.MONDAY, LocalTime.of(18, 0))),
                        new ReoccurringEventDto(
                                new ReoccurringEventTimeDto(DayOfWeek.TUESDAY, LocalTime.of(8, 0)),
                                new ReoccurringEventTimeDto(DayOfWeek.TUESDAY, LocalTime.of(11, 0))),
                        new ReoccurringEventDto(
                                new ReoccurringEventTimeDto(DayOfWeek.TUESDAY, LocalTime.of(17, 0)),
                                new ReoccurringEventTimeDto(DayOfWeek.TUESDAY, LocalTime.of(18, 0))),
                        new ReoccurringEventDto(
                                new ReoccurringEventTimeDto(DayOfWeek.THURSDAY, LocalTime.of(8, 0)),
                                new ReoccurringEventTimeDto(DayOfWeek.THURSDAY, LocalTime.of(16, 0))));
    }

}