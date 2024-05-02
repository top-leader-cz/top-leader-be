package com.topleader.topleader.user;

import com.topleader.topleader.user.manager.Manager;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;


@Getter
@Setter
@ToString(of={"username"})
@Entity
@Accessors(chain = true)
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    private String username;

    private String password;

    private String firstName;

    private String lastName;

    @Convert(converter = RoleConverter.class)
    private Set<Authority> authorities;

    private String timeZone;

    @Enumerated(EnumType.STRING)
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

    @ManyToMany(cascade = { CascadeType.MERGE })
    @JoinTable(
            joinColumns = { @JoinColumn(name = "user_username") },
            inverseJoinColumns = { @JoinColumn(name = "manager_username") }
    )
    Set<User> managers = new HashSet<>();

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
        AUTHORIZED, PENDING, PAID, REQUESTED, VIEWED, SUBMITTED

    }
}
