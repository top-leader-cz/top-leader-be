package com.topleader.topleader.feedback.repository;

import com.topleader.topleader.feedback.entity.FeedbackForm;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface FeedbackFormRepository extends CrudRepository<FeedbackForm, Long> {

    List<FeedbackForm> findByUsername(String username);
}
