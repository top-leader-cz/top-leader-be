/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach.rate;

import org.springframework.data.jpa.repository.JpaRepository;


/**
 * @author Daniel Slavik
 */
public interface CoachRateRepository extends JpaRepository<CoachRate, String> {
}
