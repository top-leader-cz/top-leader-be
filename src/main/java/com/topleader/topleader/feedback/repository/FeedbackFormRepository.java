package com.topleader.topleader.feedback.repository;

import com.topleader.topleader.feedback.entity.FeedbackForm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FeedbackFormRepository extends JpaRepository<FeedbackForm, Long> {

    @Query("select f from FeedbackForm f where f.user.username = :username")
    List<FeedbackForm> findByUsername(String username);
}
