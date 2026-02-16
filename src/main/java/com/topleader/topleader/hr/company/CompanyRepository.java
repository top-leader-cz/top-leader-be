/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.hr.company;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CompanyRepository extends ListCrudRepository<Company, Long> {

    Optional<Company> findByName(String name);

    @Modifying
    @Query("UPDATE company SET business_strategy = :strategy WHERE id = :companyId")
    void updateStrategy(long companyId, String strategy);

    @Query("SELECT rate_name FROM company_coach_rates WHERE company_id = :companyId")
    List<String> findCoachRatesByCompanyId(long companyId);

    @Modifying
    @Query("DELETE FROM company_coach_rates WHERE company_id = :companyId")
    void deleteCoachRates(long companyId);

    @Modifying
    @Query("INSERT INTO company_coach_rates (company_id, rate_name) VALUES (:companyId, :rateName)")
    void insertCoachRate(long companyId, String rateName);
}
