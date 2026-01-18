package com.topleader.topleader.user.manager;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Accessors(chain = true)
@Table("manager_view")
public class Manager {

    @Id
    private Long id;

    private String username;

    private String firstName;

    private String lastName;

    private Long companyId;
}
