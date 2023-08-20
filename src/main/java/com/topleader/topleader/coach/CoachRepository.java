/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


/**
 * @author Daniel Slavik
 */
public interface CoachRepository extends JpaRepository<Coach, String>, JpaSpecificationExecutor<Coach> {
}
