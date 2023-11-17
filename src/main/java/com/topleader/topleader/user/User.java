package com.topleader.topleader.user;


import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

    private Boolean isTrial;

    private String company;

    private String hrEmail;

    public enum Authority {
        USER,
        COACH,
        HR,
        ADMIN,
        RESPONDENT
    }

    public enum Status {
        AUTHORIZED, PENDING, PAID, REQUESTED, VIEWED, SUBMITTED

    }
}
