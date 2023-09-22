/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach.client;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;


/**
 * @author Daniel Slavik
 */
public interface CoachClientViewRepository extends JpaRepository<CoachClientView, String> {

    List<CoachClientView> findAllByCoach(String coach);
}
