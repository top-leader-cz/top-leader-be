/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.hr.company;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import java.util.HashSet;
import java.util.Set;

@Data
@Table("company")
@Accessors(chain = true)
public class Company {

    @Id
    private Long id;

    private String name;

    private String businessStrategy;

    @Transient
    private Set<String> allowedCoachRates = new HashSet<>();

    public static Company empty() {
        return new Company();
    }
}
