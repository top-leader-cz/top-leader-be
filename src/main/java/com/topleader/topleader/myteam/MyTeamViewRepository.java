/*
 * Copyright (c) 2024 Price f(x), s.r.o.
 */
package com.topleader.topleader.myteam;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


/**
 * @author Daniel Slavik
 */
public interface MyTeamViewRepository extends JpaRepository<MyTeamView, String> {

    Page<MyTeamView> findAllByManager(String manager, Pageable pageable);
}
