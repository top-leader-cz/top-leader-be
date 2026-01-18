package com.topleader.topleader.user.manager;


import com.topleader.topleader.user.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import java.util.HashSet;
import java.util.Set;

@Data
@Accessors(chain = true)
@Entity(name = "managerView")
public class Manager {

    @Id
    private Long id;

    @Column(insertable = false, updatable = false)
    private String username;

    @Column(insertable = false, updatable = false)
    private String firstName;

    @Column(insertable = false, updatable = false)
    private String lastName;

    @Column(insertable = false, updatable = false)
    private Long companyId;
}
