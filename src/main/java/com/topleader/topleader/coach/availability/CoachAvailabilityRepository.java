/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach.availability;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;


/**
 * @author Daniel Slavik
 */
public interface CoachAvailabilityRepository extends JpaRepository<CoachAvailability, Long> {

    List<CoachAvailability> findAllByUsernameAndFirstDayOfTheWeekAndRecurringIsFalse(String username, LocalDate dateFrom);

    List<CoachAvailability> findAllByUsernameAndFirstDayOfTheWeekAndDayAndRecurringIsFalse(String username, LocalDate dateFrom, DayType day);

    List<CoachAvailability> findAllByUsernameAndRecurringIsTrue(String username);

    List<CoachAvailability> findAllByUsernameAndDayAndRecurringIsTrue(String username, DayType day);

    @Modifying
    void deleteAllByUsernameAndFirstDayOfTheWeekAndRecurringIsFalse(String username, LocalDate dateFrom);

    @Modifying
    void deleteAllByUsernameAndRecurringIsTrue(String username);
}
