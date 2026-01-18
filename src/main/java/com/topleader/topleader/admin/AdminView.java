/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.admin;

import com.topleader.topleader.coach.Coach;
import com.topleader.topleader.user.RoleConverter;
import com.topleader.topleader.user.User;
import jakarta.persistence.*;

import java.security.cert.Certificate;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;


/**
 * @author Daniel Slavik
 */
@Getter
@Setter
@ToString
@Entity
@Accessors(chain = true)
@NoArgsConstructor
public class AdminView {

    @Id
    private Long id;

    @Column(insertable = false, updatable = false)
    private String username;

    @Column(insertable = false, updatable = false)
    private String firstName;

    @Column(insertable = false, updatable = false)
    private String lastName;

    @Convert(converter = RoleConverter.class)
    @Column(insertable = false, updatable = false)
    private Set<User.Authority> authorities;

    @Column(insertable = false, updatable = false)
    private String timeZone;

    @Enumerated(EnumType.STRING)
    @Column(insertable = false, updatable = false)
    private User.Status status;

    @Column(insertable = false, updatable = false)
    private Long companyId;

    @Column(insertable = false, updatable = false)
    private String companyName;

    @Column(insertable = false, updatable = false)
    private String coach;

    @Column(insertable = false, updatable = false)
    private String coachFirstName;

    @Column(insertable = false, updatable = false)
    private String coachLastName;

    @Column(insertable = false, updatable = false)
    private Integer credit;

    @Column(insertable = false, updatable = false)
    private Integer requestedCredit;

    @Column(insertable = false, updatable = false)
    private Integer sumRequestedCredit;

    @Column(insertable = false, updatable = false)
    private Integer paidCredit;

    @Column(insertable = false, updatable = false)
    private Integer scheduledCredit;

    @Column(insertable = false, updatable = false)
    private String hrs;

    @Column(insertable = false, updatable = false)
    private String requestedBy;

    @Column(insertable = false, updatable = false)
    private String freeCoach;

    @Column(insertable = false, updatable = false)
    private String locale;

    @Column(insertable = false, updatable = false)
    private String allowedCoachRates;

    @Column(insertable = false, updatable = false)
    private String rate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", insertable = false, updatable = false)
    private Set<String> certificate;

    @Column(insertable = false, updatable = false)
    private Integer internalRate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", insertable = false, updatable = false)
    private Set<Coach.PrimaryRole> primaryRoles;
}
