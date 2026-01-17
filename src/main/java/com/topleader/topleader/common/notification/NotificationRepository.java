/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.common.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.time.LocalDateTime;


/**
 * @author Daniel Slavik
 */
public interface NotificationRepository extends CrudRepository<Notification, Long>, PagingAndSortingRepository<Notification, Long> {


    boolean existsByUsernameAndTypeAndCreatedAtAfterAndReadIsFalse(
        String username,
        Notification.Type type,
        LocalDateTime startTime
    );

    Page<Notification> findByUsername(String username, Pageable pageable);

    @Modifying
    @Query("UPDATE notification SET read = true WHERE username = :username")
    void markAllAsReadForUser(String username);
}