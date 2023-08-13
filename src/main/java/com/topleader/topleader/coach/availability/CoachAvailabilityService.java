/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach.availability;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;


/**
 * @author Daniel Slavik
 */
@Service
@AllArgsConstructor
public class CoachAvailabilityService {

    private final CoachAvailabilityRepository coachAvailabilityRepository;

    public List<CoachAvailability> getWeekAvailability(
        String username,
        LocalDate dateFrom,
        AvailabilityType type
    ) {
        return switch (type) {
            case RECURRING -> coachAvailabilityRepository.findAllByUsernameAndRecurringIsTrue(username);
            case NON_RECURRING -> coachAvailabilityRepository.findAllByUsernameAndFirstDayOfTheWeekAndRecurringIsFalse(username, dateFrom);
            case ALL -> Stream.concat(
                coachAvailabilityRepository.findAllByUsernameAndRecurringIsTrue(username).stream(),
                coachAvailabilityRepository.findAllByUsernameAndFirstDayOfTheWeekAndRecurringIsFalse(username, dateFrom).stream()
            ).toList();
        };
    }

    @Transactional
    public List<CoachAvailability> setWeekAvailability(
        String username,
        LocalDate dateFrom,
        List<CoachAvailability> availabilities
    ) {
        coachAvailabilityRepository.deleteAllByUsernameAndFirstDayOfTheWeekAndRecurringIsFalse(username, dateFrom);
        return coachAvailabilityRepository.saveAll(availabilities);
    }

    @Transactional
    public List<CoachAvailability> setRecurringAvailability(
        String username,
        List<CoachAvailability> availabilities
    ) {
        coachAvailabilityRepository.deleteAllByUsernameAndRecurringIsTrue(username);
        return coachAvailabilityRepository.saveAll(availabilities);
    }
}
