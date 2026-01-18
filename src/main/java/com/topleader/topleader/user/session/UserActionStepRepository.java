/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.user.session;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;


/**
 * @author Daniel Slavik
 */
public interface UserActionStepRepository extends ListCrudRepository<UserActionStep, Long> {

    @Query("SELECT * FROM user_action_step WHERE username = :username")
    List<UserActionStep> findAllByUsername(String username);

    @Query("SELECT * FROM user_action_step WHERE username = :username AND checked = false ORDER BY date DESC LIMIT 1")
    Optional<UserActionStep> findFirstByUsernameAndCheckedIsFalseOrderByDateDesc(String username);
}
