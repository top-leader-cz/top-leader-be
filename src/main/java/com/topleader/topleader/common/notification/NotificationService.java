/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.common.notification;

import com.topleader.topleader.common.notification.context.NotificationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void markAllNotificationsAsReadForUser(String username) {
        notificationRepository.markAllAsReadForUser(username);
    }

    public Page<Notification> fetchUserNotifications(String username, Pageable pageable) {
        return notificationRepository.findByUsername(username, pageable);
    }

    public Notification addNotification(CreateNotificationRequest request) {

        final var now = LocalDateTime.now();

        if (notificationRepository.existsByUsernameAndTypeAndCreatedAtAfterAndReadIsFalse(
            request.username(),
            request.type().name(),
            now.minusHours(1)
        )) {
            log.info("The same unread notification already exists. Skipping creation of the {}", request);
        }

        return notificationRepository.save(new Notification()
            .setUsername(request.username())
            .setType(request.type())
            .setContext(request.context())
            .setCreatedAt(now)
            .setRead(false)
        );
    }

    public record CreateNotificationRequest(
        String username,
        Notification.Type type,
        NotificationContext context
    ) {
    }
}

