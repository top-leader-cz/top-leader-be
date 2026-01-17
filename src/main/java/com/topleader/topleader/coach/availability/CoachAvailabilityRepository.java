/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach.availability;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;


/**
 * @author Daniel Slavik
 */
public interface CoachAvailabilityRepository extends CrudRepository<CoachAvailability, Long>, PagingAndSortingRepository<CoachAvailability, Long> {

    List<CoachAvailability> findAllByUsernameAndRecurringIsTrue(String username);

    @Query("SELECT * FROM coach_availability WHERE username = :username AND date_time_from <= :to AND date_time_to >= :from")
    List<CoachAvailability> findAllByUsernameAndDateTimeFromAfterAndDateTimeToBefore(String username, LocalDateTime from, LocalDateTime to);
}
