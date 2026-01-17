package com.topleader.topleader.user.manager;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("users_managers")
@Accessors(chain = true)
public class UsersManagers {

    private String userUsername;

    private String managerUsername;
}
