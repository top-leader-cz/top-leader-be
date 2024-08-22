/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.company;

import jakarta.persistence.*;
import java.util.Set;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;


/**
 * @author Daniel Slavik
 */
@Getter
@Setter
@ToString
@Entity
@Accessors(chain = true)
@NoArgsConstructor
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "company_id_seq")
    @SequenceGenerator(name = "company_id_seq", sequenceName = "company_id_seq", allocationSize = 1)
    private Long id;

    private String name;

    private String businessStrategy;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "company_coach_rates", joinColumns = @JoinColumn(name = "company_id"))
    @Column(name = "rate_name")
    private Set<String> allowedCoachRates;

    public static Company empty() {
        return new Company();
    }
}
