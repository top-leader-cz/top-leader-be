/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


/**
 * @author Daniel Slavik
 */
public interface CoachImageRepository extends JpaRepository<CoachImage, Long> {
    Optional<CoachImage> findByUsername(String username);
}
