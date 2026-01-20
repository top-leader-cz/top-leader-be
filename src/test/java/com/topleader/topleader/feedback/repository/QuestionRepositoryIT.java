/*
 * Copyright (c) 2024 Price f(x), s.r.o.
 */
package com.topleader.topleader.feedback.repository;

import com.topleader.topleader.IntegrationTest;
import com.topleader.topleader.feedback.entity.Question;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

@Sql(scripts = {"/sql/feedback/question-repository-test.sql"})
class QuestionRepositoryIT extends IntegrationTest {

    @Autowired
    private QuestionRepository questionRepository;

    @Test
    void shouldCheckIfQuestionExistsByKey() {
        // Test existsByKey with existing question
        var exists = questionRepository.existsByKey("test.question.1");
        assertThat("Question with key 'test.question.1' should exist", exists, is(true));

        // Test existsByKey with non-existing question
        var notExists = questionRepository.existsByKey("non.existing.key");
        assertThat("Question with key 'non.existing.key' should not exist", notExists, is(false));
    }

    @Test
    void shouldSaveNewQuestion() {
        // Given a new question
        var newQuestion = new Question()
                .setKey("new.test.question")
                .setDefaultQuestion(false);

        // When saving it
        var saved = questionRepository.save(newQuestion);

        // Then it should be saved with generated ID
        assertThat("Saved question should have an ID", saved.getId(), notNullValue());

        // And existsByKey should return true
        var exists = questionRepository.existsByKey("new.test.question");
        assertThat("Newly saved question should exist", exists, is(true));
    }

    @Test
    void shouldGetDefaultQuestions() {
        // When fetching default questions
        var defaultQuestions = questionRepository.getDefaultOptions();

        // Then should return only default questions
        assertThat("Should have at least one default question", defaultQuestions.size() >= 1, is(true));
        assertThat("All questions should be default",
                defaultQuestions.stream().allMatch(Question::isDefaultQuestion), is(true));
    }
}
