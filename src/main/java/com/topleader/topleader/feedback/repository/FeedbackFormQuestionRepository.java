package com.topleader.topleader.feedback.repository;

import com.topleader.topleader.feedback.entity.FeedbackFormQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackFormQuestionRepository extends JpaRepository<FeedbackFormQuestion, FeedbackFormQuestion.FeedbackFormQuestionId> {


}
