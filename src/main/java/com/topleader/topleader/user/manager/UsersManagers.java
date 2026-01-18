package com.topleader.topleader.user.manager;

import com.topleader.topleader.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Entity
@Accessors(chain = true)
public class UsersManagers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_username")
    private String userUsername;

    @Column(name = "manager_username")
    private String managerUsername;

    @ManyToOne
    @JoinColumn(name = "user_username", referencedColumnName = "username", insertable = false, updatable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "manager_username", referencedColumnName = "username", insertable = false, updatable = false)
    private User manager;
}
