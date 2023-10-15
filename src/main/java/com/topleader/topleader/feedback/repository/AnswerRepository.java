package com.topleader.topleader.feedback.repository;

import com.topleader.topleader.feedback.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
}
