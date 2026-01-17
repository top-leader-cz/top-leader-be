/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.hr.company;

import java.util.Optional;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;


/**
 * @author Daniel Slavik
 */
public interface CompanyRepository extends CrudRepository<Company, Long> {

    Optional<Company> findByName(String name);

    @Modifying
    @Query("update company set business_strategy = :strategy where id = :companyId")
    void updateStrategy(long companyId, String strategy);
}
