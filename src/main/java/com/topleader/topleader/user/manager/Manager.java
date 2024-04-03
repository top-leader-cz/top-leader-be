package com.topleader.topleader.user.manager;


import com.topleader.topleader.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import lombok.Data;
import lombok.experimental.Accessors;
import java.util.HashSet;
import java.util.Set;

@Data
@Accessors(chain = true)
@Entity(name = "managerView")
public class Manager {

    @Id
    private String username;

    @Column(insertable = false, updatable = false)
    private String firstName;

    @Column(insertable = false, updatable = false)
    private String lastName;

    @Column(insertable = false, updatable = false)
    private Long companyId;
}
