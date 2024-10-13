/*
 * Copyright (c) 2024 Price f(x), s.r.o.
 */
package com.topleader.topleader.feedback_notification;

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
public class FeedbackNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "feedback_notification_id_seq")
    @SequenceGenerator(name = "feedback_notification_id_seq", sequenceName = "feedback_notification_id_seq", allocationSize = 1)
    private Long id;

    private String username;

    private LocalDateTime createdAt;

    private LocalDateTime notificationTime;

    private Long feedbackFormId;

    private LocalDateTime processedTime;

    private LocalDateTime manualAvailableAfter;

    private LocalDateTime manualReminderSentTime;

    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Status {
        NEW,
        PROCESSED,
        MANUAL_SENT
    }

}
