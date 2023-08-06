package com.topleader.topleader.user;


import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Set;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Entity
@Accessors(chain = true)
@Table(name = "users")
public class User {

    @Id
    private String username;

    private String password;

    private boolean enabled;

    @Convert(converter = RoleConverter.class)
    private Set<Authority> authorities;

    public enum Authority {
        USER,
        COACH
    }
}
