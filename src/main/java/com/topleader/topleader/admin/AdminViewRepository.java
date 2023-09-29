/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.admin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


/**
 * @author Daniel Slavik
 */
public interface AdminViewRepository extends JpaRepository<AdminView, String>, JpaSpecificationExecutor<AdminView> {
}
