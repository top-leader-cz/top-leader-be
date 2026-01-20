package com.topleader.topleader.feedback.repository;

import com.topleader.topleader.feedback.entity.FeedbackFormAnswer;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.Optional;

public interface FeedbackFormAnswerRepository extends ListCrudRepository<FeedbackFormAnswer, Long> {

    List<FeedbackFormAnswer> findByFormId(long formId);

    List<FeedbackFormAnswer> findByRecipientId(long recipientId);

    Optional<FeedbackFormAnswer> findByFormIdAndRecipientIdAndQuestionKey(long formId, long recipientId, String questionKey);

    void deleteByFormId(long formId);

    void deleteByRecipientId(long recipientId);
}
