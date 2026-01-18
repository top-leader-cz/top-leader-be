package com.topleader.topleader.feedback.repository;

import com.topleader.topleader.feedback.entity.FeedbackFormQuestion;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface FeedbackFormQuestionRepository extends CrudRepository<FeedbackFormQuestion, Long> {

    List<FeedbackFormQuestion> findByFeedbackFormId(long feedbackFormId);

    Optional<FeedbackFormQuestion> findByFeedbackFormIdAndQuestionKey(long feedbackFormId, String questionKey);

    void deleteByFeedbackFormId(long feedbackFormId);
}
