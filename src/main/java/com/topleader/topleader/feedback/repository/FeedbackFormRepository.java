package com.topleader.topleader.feedback.repository;

import com.topleader.topleader.feedback.entity.FeedbackForm;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackFormRepository extends JpaRepository<FeedbackForm, Long> {
}
