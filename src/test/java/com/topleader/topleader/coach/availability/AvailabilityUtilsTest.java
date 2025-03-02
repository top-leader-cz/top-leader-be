package com.topleader.topleader.coach.availability;

import com.topleader.topleader.TestUtils;

import com.topleader.topleader.coach.availability.domain.ReoccurringEventDto;
import com.topleader.topleader.coach.availability.domain.ReoccurringEventTimeDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.DayOfWeek;
import java.time.LocalTime;


class AvailabilityUtilsTest {

    @Test
    public void test() throws URISyntaxException, IOException {
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