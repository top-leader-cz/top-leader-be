/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.user.session;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;


/**
 * @author Daniel Slavik
 */
public interface UserActionStepRepository extends JpaRepository<UserActionStep, Long> {

    List<UserActionStep> findAllByUsername(String username);

    Optional<UserActionStep>  findFirstByUsernameAndCheckedIsFalseOrderByDateDesc(String username);
}
