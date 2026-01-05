/*
 * Copyright (c) 2024 Price f(x), s.r.o.
 */
package com.topleader.topleader.feedback.feedback_notification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;


/**
 * @author Daniel Slavik
 */
public interface FeedbackNotificationRepository extends JpaRepository<FeedbackNotification, Long> {

    List<FeedbackNotification> findAllByStatusAndNotificationTimeBefore(FeedbackNotification.Status status, LocalDateTime notificationTime);

    Optional<FeedbackNotification> findByFeedbackFormId(long formId);

}
