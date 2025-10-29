/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach;

import com.topleader.topleader.user.User;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.Optional;
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

    private boolean publicProfile;

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

    private int priority;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Set<PrimaryRole> primaryRoles;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "username", referencedColumnName = "username")
    private User user;

    @Enumerated(EnumType.STRING)
    private CertificateType certificate;

    public enum CertificateType {
        ACC,
        PCC,
        MCC
    }


    public enum PrimaryRole {
        COACH,
        MENTOR,
        TRAINER,
        FACILITATOR,
        CONSULTANT,
        SPEAKER
    }

    public String getUserEmail() {
        return Optional.ofNullable(user)
                .map(User::getEmail)
                .orElse(username);
    }
}
