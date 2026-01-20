/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach.list;

import com.fasterxml.jackson.core.type.TypeReference;
import com.topleader.topleader.coach.Coach;
import com.topleader.topleader.common.util.common.JsonUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.util.Set;


/**
 * @author Daniel Slavik
 */
@Getter
@Setter
@ToString
@Table("coach_list_view")
@Accessors(chain = true)
@NoArgsConstructor
public class CoachListView {

    @Id
    private String username;

    private Boolean publicProfile;

    private String firstName;

    private String lastName;

    private String email;

    private String webLink;

    private String bio;

    private String languages;

    private String timeZone;

    private String fields;

    private LocalDate experienceSince;

    private String rate;

    private Integer rateOrder;

    private String linkedinProfile;

    private String primaryRoles;

    private String certificate;

    private int priority;

    public Set<String> getLanguagesSet() {
        if (languages == null || languages.isBlank()) return Set.of();
        return JsonUtils.fromJsonString(languages, new TypeReference<>() {});
    }

    public CoachListView setLanguagesSet(Set<String> v) {
        this.languages = JsonUtils.toJsonString(v);
        return this;
    }

    public Set<String> getFieldsSet() {
        if (fields == null || fields.isBlank()) return Set.of();
        return JsonUtils.fromJsonString(fields, new TypeReference<>() {});
    }

    public CoachListView setFieldsSet(Set<String> v) {
        this.fields = JsonUtils.toJsonString(v);
        return this;
    }

    public Set<Coach.PrimaryRole> getPrimaryRolesSet() {
        if (primaryRoles == null || primaryRoles.isBlank()) return Set.of();
        return JsonUtils.fromJsonString(primaryRoles, new TypeReference<>() {});
    }

    public CoachListView setPrimaryRolesSet(Set<Coach.PrimaryRole> v) {
        this.primaryRoles = JsonUtils.toJsonString(v);
        return this;
    }

    public Set<String> getCertificateSet() {
        if (certificate == null || certificate.isBlank()) return Set.of();
        return JsonUtils.fromJsonString(certificate, new TypeReference<>() {});
    }

    public CoachListView setCertificateSet(Set<String> v) {
        this.certificate = JsonUtils.toJsonString(v);
        return this;
    }
}
