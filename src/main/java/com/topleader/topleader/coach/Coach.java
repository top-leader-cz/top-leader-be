/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach;

import jakarta.persistence.*;

import java.time.LocalDate;
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
public class Coach {

    @Id
    private String username;

    private Boolean publicProfile;

    private String email;

    @Column(length = 1000)
    private String webLink;

    @Column(length = 1000)
    private String bio;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> languages;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> fields;

    private LocalDate experienceSince;

    private String rate;

    private Integer rateOrder;

    private Integer internalRate;

    private String linkedinProfile;

    private boolean freeSlots;

    @Enumerated(EnumType.STRING)
    private CertificateType certificate;

    public enum CertificateType {
        ACC,
        PCC,
        MCC
    }
}
