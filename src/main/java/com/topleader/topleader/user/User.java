package com.topleader.topleader.user;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

}
