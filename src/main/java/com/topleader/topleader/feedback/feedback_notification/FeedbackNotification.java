/*
 * Copyright (c) 2024 Price f(x), s.r.o.
 */
package com.topleader.topleader.feedback.feedback_notification;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;


/**
 * @author Daniel Slavik
 */
@Getter
@Setter
@ToString
@Table("feedback_notification")
@Accessors(chain = true)
@NoArgsConstructor
public class FeedbackNotification {

    @Id
    private Long id;

    private String username;

    private LocalDateTime createdAt;

    private LocalDateTime notificationTime;

    private Long feedbackFormId;

    private LocalDateTime processedTime;

    private LocalDateTime manualAvailableAfter;

    private LocalDateTime manualReminderSentTime;

    private Status status;

    public enum Status {
        NEW,
        PROCESSED,
        MANUAL_SENT
    }

}
