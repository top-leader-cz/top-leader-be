/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.company;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;


/**
 * @author Daniel Slavik
 */
public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByName(String name);

    @Modifying
    @Query("update Company c set c.businessStrategy = :strategy where c.id = :companyId")
    void updateStrategy(long companyId, String strategy);

}
