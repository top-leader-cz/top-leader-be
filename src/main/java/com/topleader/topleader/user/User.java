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

    private boolean enabled;

    @Convert(converter = RoleConverter.class)
    private Set<Authority> authorities;

    private String timeZone;

    @OneToOne(cascade ={CascadeType.PERSIST, CascadeType.MERGE})
    private UserInfo userInfo;

    public enum Authority {
        USER,
        COACH
    }
}
