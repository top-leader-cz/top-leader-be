/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.user.assessment;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;


/**
 * @author Daniel Slavik
 */
public interface UserAssessmentRepository extends JpaRepository<UserAssessment, Long> {

    List<UserAssessment> findAllByUsername(String username);

    Optional<UserAssessment> findByUsernameAndQuestionId(String username, Long questionId);

    @Modifying
    void deleteAllByUsername(String username);
}
