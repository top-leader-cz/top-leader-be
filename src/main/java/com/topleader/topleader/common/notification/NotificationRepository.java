/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.common.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.time.LocalDateTime;
import java.util.List;


public interface NotificationRepository extends ListCrudRepository<Notification, Long>,
                                                  PagingAndSortingRepository<Notification, Long> {

    @Query("""
        SELECT COUNT(*) > 0 FROM notification
        WHERE username = :username
        AND type = :type
        AND created_at > :startTime
        AND read = false
        """)
    boolean existsByUsernameAndTypeAndCreatedAtAfterAndReadIsFalse(
        String username,
        String type,
        LocalDateTime startTime
    );

    List<Notification> findByUsername(String username);

    Page<Notification> findByUsername(String username, Pageable pageable);

    @Modifying
    @Query("UPDATE notification SET read = true WHERE username = :username")
    void markAllAsReadForUser(String username);
}