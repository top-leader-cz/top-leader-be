/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.company;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;


/**
 * @author Daniel Slavik
 */
public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByName(String name);

}
