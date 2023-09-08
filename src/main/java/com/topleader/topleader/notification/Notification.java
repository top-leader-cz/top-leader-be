/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.notification;

import com.topleader.topleader.notification.context.NotificationContext;
import com.topleader.topleader.notification.context.NotificationContextConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;


/**
 * @author Daniel Slavik
 */
@Getter
@Setter
@ToString
@Entity
@Accessors(chain = true)
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notification_id_seq")
    @SequenceGenerator(name = "notification_id_seq", sequenceName = "notification_id_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Type type;

    @Column(nullable = false)
    private boolean read;

    @Convert(converter = NotificationContextConverter.class)
    @Column(columnDefinition = "TEXT")
    private NotificationContext context;

    private LocalDateTime createdAt;

    public enum Type {
        MESSAGE
    }

}
