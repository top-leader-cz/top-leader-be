package com.topleader.topleader.feedback.repository;

import com.topleader.topleader.feedback.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query("SELECT q FROM fb_question q WHERE q.defaultQuestion = true")
    List<Question> getOptions();
}
