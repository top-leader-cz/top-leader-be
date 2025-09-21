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
    private String username;

    private String firstName;

    private String lastName;

    @Convert(converter = RoleConverter.class)
    private Set<User.Authority> authorities;

    private String timeZone;

    @Enumerated(EnumType.STRING)
    private User.Status status;

    private Long companyId;

    private String companyName;

    private String coach;

    private String coachFirstName;

    private String coachLastName;

    private Integer credit;

    private Integer requestedCredit;

    private Integer sumRequestedCredit;

    private Integer paidCredit;

    private Integer scheduledCredit;

    private String hrs;

    private String requestedBy;

    private String freeCoach;

    private String locale;

    private String allowedCoachRates;

    private String rate;

    @Enumerated(EnumType.STRING)
    private Coach.CertificateType certificate;

    private Integer internalRate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Set<Coach.PrimaryRole> primaryRoles;
}
