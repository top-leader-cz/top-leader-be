package com.topleader.topleader.user;


import com.topleader.topleader.user.userinfo.UserInfo;
import jakarta.persistence.*;

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

    private boolean enabled;

    @Convert(converter = RoleConverter.class)
    private Set<Authority> authorities;

    private String timeZone;

    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Authority {
        USER,
        COACH
    }

    public enum Status {
        AUTHORIZED, PENDING, PAID

    }
}
