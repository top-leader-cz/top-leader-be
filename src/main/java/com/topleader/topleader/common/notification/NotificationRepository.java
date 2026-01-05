/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.common.notification;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;


/**
 * @author Daniel Slavik
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {


    boolean existsByUsernameAndTypeAndCreatedAtAfterAndReadIsFalse(
        String username,
        Notification.Type type,
        LocalDateTime startTime
    );

    Page<Notification> findByUsername(String username, Pageable pageable);

    @Transactional
    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.username = :username")
    void markAllAsReadForUser(String username);
}