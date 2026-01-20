/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.admin;

import com.topleader.topleader.coach.Coach;
import com.topleader.topleader.user.User;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Set;


@Data
@Accessors(chain = true)
@Table("admin_view")
public class AdminView {

    @Id
    private Long id;

    private String username;

    private String firstName;

    private String lastName;

    private Set<User.Authority> authorities;

    private String timeZone;

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

    private Set<String> certificate;

    private Integer internalRate;

    private Set<Coach.PrimaryRole> primaryRoles;
}
