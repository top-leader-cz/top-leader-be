package com.topleader.topleader.user;

import com.topleader.topleader.common.util.common.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;


@Getter
@Setter
@ToString(of={"username"})
@Table("users")
@Accessors(chain = true)
@NoArgsConstructor
public class User {

    @Id
    private Long id;

    private String username;

    private String password;

    private String firstName;

    private String lastName;

    private String email;

    private String authorities;

    private Status status;

    private Long companyId;

    private String coach;

    private Integer credit;

    private Integer scheduledCredit;

    private Integer requestedCredit;

    private Integer paidCredit;

    private Integer sumRequestedCredit;

    private String requestedBy;

    private String position;

    private String hrEmail;

    private String freeCoach;

    private String locale;

    private String aspiredCompetency;

    private String aspiredPosition;

    private String timeZone;

    @Transient
    private Set<UserCoachRate> userCoachRates = new HashSet<>();

    public Set<Authority> getAuthorities() {
        if (authorities == null) {
            return Set.of(Authority.USER);
        }
        return JsonUtils.fromJsonString(authorities, new TypeReference<Set<Authority>>() {});
    }

    public User setAuthorities(Set<Authority> authorities) {
        this.authorities = authorities != null ? JsonUtils.toJsonString(authorities) : null;
        return this;
    }

    public Set<String> getAllowedCoachRates() {
        return userCoachRates.stream()
                .map(UserCoachRate::getRateName)
                .collect(Collectors.toSet());
    }

    public User setAllowedCoachRates(Set<String> rates) {
        this.userCoachRates = rates != null
                ? rates.stream().map(UserCoachRate::new).collect(Collectors.toSet())
                : new HashSet<>();
        return this;
    }

    public static User empty() {
        return new User()
            .setUsername("")
            .setFirstName("")
            .setLastName("")
            .setTimeZone("")
            .setCompanyId(0L)
            .setAuthorities(Set.of())
            .setStatus(Status.PENDING)
            .setLocale("en");
    }

    public enum Authority {
        RESPONDENT,
        USER,
        MANAGER,
        COACH,
        HR,
        ADMIN,
    }

    public enum Status {
        AUTHORIZED, PENDING, PAID, REQUESTED, VIEWED, SUBMITTED, CANCELED

    }
}
