/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.admin;

import com.topleader.topleader.user.RoleConverter;
import com.topleader.topleader.user.User;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
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

    private String hrs;

    private String requestedBy;

    private Boolean isTrial;

    private String freeCoach;

    private String locale;
}
