package com.topleader.topleader.feedback.repository;

import com.topleader.topleader.feedback.entity.FeedbackFormAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackFormAnswerRepository extends JpaRepository<FeedbackFormAnswer, FeedbackFormAnswer.FeedbackFormAnswerId> {

}
