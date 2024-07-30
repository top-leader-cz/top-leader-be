package com.topleader.topleader.feedback;

import com.topleader.topleader.IntegrationTest;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import static org.junit.jupiter.api.Assertions.*;

class FeedbackServiceIT extends IntegrationTest {

    @Autowired
    private FeedbackService feedbackService;

    @Test
    @Sql(scripts = {"/feedback/sql/feedback.sql", "/feedback/sql/feedback-answers.sql"})
    @Transactional
    void generateSummary() {
        feedbackService.generateSummary(1L);
    }
}