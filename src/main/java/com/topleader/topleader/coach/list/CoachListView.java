/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach.list;

import com.fasterxml.jackson.core.type.TypeReference;
import com.topleader.topleader.coach.Coach;
import com.topleader.topleader.common.util.common.JsonUtils;

import java.time.LocalDate;
import java.util.HashSet;
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
@Accessors(chain = true)
@NoArgsConstructor
public class CoachListView {

    private String username;

    private Boolean publicProfile;

    private String firstName;

    private String lastName;

    private String email;

    private String webLink;

    private String bio;

    private Set<String> languages;

    private String timeZone;

    private Set<String> fields;

    private LocalDate experienceSince;

    private String rate;

    private Integer rateOrder;

    private String linkedinProfile;

    private String primaryRoles;

    private String certificate;

    private int priority;

    public Set<Coach.PrimaryRole> getPrimaryRolesSet() {
        return primaryRoles != null
                ? JsonUtils.fromJsonString(primaryRoles, new TypeReference<Set<Coach.PrimaryRole>>() {})
                : new HashSet<>();
    }

    public CoachListView setPrimaryRolesSet(Set<Coach.PrimaryRole> roles) {
        this.primaryRoles = roles != null ? JsonUtils.toJsonString(roles) : null;
        return this;
    }

    public Set<String> getCertificateSet() {
        return certificate != null
                ? JsonUtils.fromJsonString(certificate, new TypeReference<Set<String>>() {})
                : new HashSet<>();
    }

    public CoachListView setCertificateSet(Set<String> certs) {
        this.certificate = certs != null ? JsonUtils.toJsonString(certs) : null;
        return this;
    }

}
