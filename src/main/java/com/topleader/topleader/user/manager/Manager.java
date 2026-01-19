package com.topleader.topleader.user.manager;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.data.relational.core.mapping.Table;
import com.topleader.topleader.common.entity.BaseEntity;

@Data
@EqualsAndHashCode(callSuper=false)
@Accessors(chain = true)
@Table("manager_view")
public class Manager extends BaseEntity {
    private String username;

    private String firstName;

    private String lastName;

    private Long companyId;
}
