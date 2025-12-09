/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach.list;

import com.topleader.topleader.coach.Coach;
import jakarta.persistence.*;

import java.time.LocalDate;
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
public class CoachListView {

    @Id
    private String username;

    private Boolean publicProfile;

    private String firstName;

    private String lastName;

    private String email;

    @Column(length = 1000)
    private String webLink;

    @Column(length = 1000)
    private String bio;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "coach_languages", joinColumns = @JoinColumn(name = "coach_username"))
    private Set<String> languages;

    private String timeZone;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "coach_fields", joinColumns = @JoinColumn(name = "coach_username"))
    private Set<String> fields;

    private LocalDate experienceSince;

    private String rate;

    private Integer rateOrder;

    @Column(length = 1000)
    private String linkedinProfile;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Set<Coach.PrimaryRole> primaryRoles;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Set<String> certificate;

    private int priority;


}
