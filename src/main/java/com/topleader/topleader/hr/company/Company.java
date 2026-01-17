/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.hr.company;

import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;


/**
 * @author Daniel Slavik
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@NoArgsConstructor
@Table("company")
public class Company {

    @Id
    private Long id;

    private String name;

    private String businessStrategy;

    @MappedCollection(idColumn = "company_id")
    private Set<CompanyCoachRate> allowedCoachRates = new HashSet<>();

    public static Company empty() {
        return new Company();
    }

    public Set<String> getAllowedCoachRateNames() {
        return allowedCoachRates.stream()
            .map(CompanyCoachRate::getRateName)
            .collect(java.util.stream.Collectors.toSet());
    }

    public void setAllowedCoachRateNames(Set<String> rateNames) {
        this.allowedCoachRates = rateNames.stream()
            .map(name -> new CompanyCoachRate().setRateName(name))
            .collect(java.util.stream.Collectors.toSet());
    }
}
