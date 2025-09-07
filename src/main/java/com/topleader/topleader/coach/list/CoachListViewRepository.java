/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach.list;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


/**
 * @author Daniel Slavik
 */
public interface CoachListViewRepository extends JpaRepository<CoachListView, String>, JpaSpecificationExecutor<CoachListView> {

}
