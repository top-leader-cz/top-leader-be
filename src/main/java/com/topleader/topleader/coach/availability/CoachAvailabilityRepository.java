/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach.availability;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


/**
 * @author Daniel Slavik
 */
public interface CoachAvailabilityRepository extends JpaRepository<CoachAvailability, Long> {

    List<CoachAvailability> findAllByUsernameAndRecurringIsTrue(String username);

    @Query("SELECT c FROM CoachAvailability c WHERE c.username = :username AND c.dateTimeFrom <= :to AND c.dateTimeTo >= :from")
    List<CoachAvailability> findAllByUsernameAndDateTimeFromAfterAndDateTimeToBefore(String username, LocalDateTime from, LocalDateTime to);
}
