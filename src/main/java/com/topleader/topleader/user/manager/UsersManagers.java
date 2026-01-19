package com.topleader.topleader.user.manager;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.data.relational.core.mapping.Table;
import com.topleader.topleader.common.entity.BaseEntity;

@Data
@EqualsAndHashCode(callSuper=false)
@Table("users_managers")
@Accessors(chain = true)
public class UsersManagers extends BaseEntity {
    private String userUsername;

    private String managerUsername;
}
