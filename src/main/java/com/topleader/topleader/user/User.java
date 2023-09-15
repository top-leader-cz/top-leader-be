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
@ToString
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

    private String firstName;

    private String lastName;

    private Long companyId;

    private String coach;

    private Integer credit;

    private Integer requestedCredit;

    private Boolean isTrial;

    public enum Authority {
        USER,
        COACH,
        HR
    }

    public enum Status {
        AUTHORIZED, PENDING, PAID

    }
}
