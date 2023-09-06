/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.notification;

import com.topleader.topleader.notification.context.NotificationContext;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * @author Daniel Slavik
 */
@RestController
@RequestMapping("/api/latest/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public Page<NotificationDto> fetchUserNotifications(
        @AuthenticationPrincipal UserDetails user,
        Pageable pageable
    ) {
        return notificationService.fetchUserNotifications(user.getUsername(), pageable)
            .map(NotificationDto::from);
    }

    @PostMapping("/mark-as-read")
    public void markAllNotificationsAsRead(@AuthenticationPrincipal UserDetails user) {
        notificationService.markAllNotificationsAsReadForUser(user.getUsername());
    }

    public record NotificationDto(
        Long id,
        String username,
        Notification.Type type,
        boolean read,
        NotificationContext context,
        LocalDateTime createdAt
    ) {
        public static NotificationDto from(Notification notification) {
            return new NotificationDto(
                notification.getId(),
                notification.getUsername(),
                notification.getType(),
                notification.isRead(),
                notification.getContext(),
                notification.getCreatedAt()
            );
        }
    }
}

