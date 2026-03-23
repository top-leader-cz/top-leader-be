package com.topleader.topleader.admin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.topleader.topleader.coach.Coach;
import com.topleader.topleader.common.util.common.JsonbValue;
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

    @JsonIgnore
    private JsonbValue certificate;

    private Integer internalRate;

    @JsonIgnore
    private JsonbValue primaryRoles;

    private static final TypeReference<Set<Coach.PrimaryRole>> PRIMARY_ROLES_TYPE = new TypeReference<>() {};

    @JsonProperty("certificate")
    public Set<String> getCertificateSet() {
        return certificate != null && !certificate.isNull() ? JsonbValue.toStringSet(certificate) : null;
    }
    public AdminView setCertificateSet(Set<String> v) { this.certificate = JsonbValue.fromSet(v); return this; }

    @JsonProperty("primaryRoles")
    public Set<Coach.PrimaryRole> getPrimaryRolesSet() {
        return primaryRoles != null && !primaryRoles.isNull() ? JsonbValue.toSet(primaryRoles, PRIMARY_ROLES_TYPE) : null;
    }
    public AdminView setPrimaryRolesSet(Set<Coach.PrimaryRole> v) { this.primaryRoles = JsonbValue.fromSet(v); return this; }
}
