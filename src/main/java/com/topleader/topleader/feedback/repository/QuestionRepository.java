package com.topleader.topleader.feedback.repository;

import com.topleader.topleader.feedback.entity.Question;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface QuestionRepository extends CrudRepository<Question, Long> {

    @Query("SELECT * FROM fb_question WHERE default_question = true")
    List<Question> getDefaultOptions();

    @Query("SELECT COUNT(*) > 0 FROM fb_question WHERE key = :key")
    boolean existsByKey(String key);
}
