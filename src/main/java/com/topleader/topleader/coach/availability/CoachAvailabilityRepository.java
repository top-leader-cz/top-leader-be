/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach.availability;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;


/**
 * @author Daniel Slavik
 */
public interface CoachAvailabilityRepository extends JpaRepository<CoachAvailability, Long> {

    List<CoachAvailability> findAllByUsernameAndRecurringIsTrue(String username);

    List<CoachAvailability> findAllByUsernameAndDateTimeFromAfterAndDateTimeToBefore(String username, LocalDateTime from, LocalDateTime to);

    List<CoachAvailability.AvailabilityType> getAvailabilityTpe(String username);
}
