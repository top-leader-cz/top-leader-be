/*
 * Copyright (c) 2024 Price f(x), s.r.o.
 */
package com.topleader.topleader.google;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;


/**
 * @author Daniel Slavik
 */
public interface SyncEventRepository extends JpaRepository<SyncEvent, Long> {
    @Modifying
    void deleteAllByUsername(String username);

    @Modifying
    void deleteAllByUsernameAndExternalIdIn(String username, List<String> toDelete);

    List<SyncEvent> findAllByUsername(String username);

    @Query("select se from SyncEvent se where se.username = :username and se.startDate between :startDate and :endDate and se.endDate between :startDate and :endDate")
    List<SyncEvent> findAllByUsernameAndStartDateAndEndDateBetween(String username, LocalDateTime startDate, LocalDateTime endDate);
}
