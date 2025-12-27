package com.topleader.topleader.coach.availability;

import tools.jackson.databind.JsonNode;
import com.topleader.topleader.coach.availability.domain.ReoccurringEventDto;
import com.topleader.topleader.coach.availability.domain.ReoccurringEventTimeDto;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;

import java.util.List;

@UtilityClass
public class AvailabilityUtils {

    public List<ReoccurringEventDto> toReoccurringEvent(JsonNode data) {
        var evetns = new ArrayList<>(List.of(new ReoccurringEventDto(null, null)));
        data.get("collection").forEach(node -> {
            var startTime = node.get("start_time").textValue();
            var status = node.get("status").textValue();
            if ("available".equals(status)) {
                var date = ZonedDateTime.parse(startTime).toLocalDateTime();
                var dayOfTheWeek = date.getDayOfWeek();
                var last = evetns.getLast();
                if(last.from() == null) {
                    evetns.remove(last);
                    evetns.add(new ReoccurringEventDto(eventTime(date), eventTime(date)));
                } else if (last.to().time().plusMinutes(61).isAfter(date.toLocalTime()) && last.from().day() == dayOfTheWeek) {
                    evetns.remove(last);
                    evetns.add(new ReoccurringEventDto(last.from(), eventTime(date.plusHours(1))));
                } else {
                    evetns.add(new ReoccurringEventDto(eventTime(date), eventTime(date.plusHours(1))));
                }

            }
        });

        return evetns;
    }


    private ReoccurringEventTimeDto eventTime(LocalDateTime date) {
        return new ReoccurringEventTimeDto(date.getDayOfWeek(), date.toLocalTime());
    }

}
