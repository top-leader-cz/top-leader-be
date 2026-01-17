/*
 * Copyright (c) 2024 Price f(x), s.r.o.
 */
package com.topleader.topleader.myteam;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;


/**
 * @author Daniel Slavik
 */
public interface MyTeamViewRepository extends CrudRepository<MyTeamView, String>, PagingAndSortingRepository<MyTeamView, String> {

    Page<MyTeamView> findAllByManager(String manager, Pageable pageable);
}
