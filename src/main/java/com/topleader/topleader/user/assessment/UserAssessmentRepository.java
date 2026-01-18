/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.user.assessment;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;


/**
 * @author Daniel Slavik
 */
public interface UserAssessmentRepository extends ListCrudRepository<UserAssessment, Long> {

    @Query("SELECT * FROM user_assessment WHERE username = :username")
    List<UserAssessment> findAllByUsername(String username);

    @Query("SELECT * FROM user_assessment WHERE username = :username AND question_id = :questionId")
    Optional<UserAssessment> findByUsernameAndQuestionId(String username, Long questionId);

    @Modifying
    @Query("DELETE FROM user_assessment WHERE username = :username")
    void deleteAllByUsername(String username);
}
