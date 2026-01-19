/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.session.user_allocation;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import com.topleader.topleader.common.entity.BaseEntity;

@Data
@EqualsAndHashCode(callSuper=false)
@Accessors(chain = true)
@NoArgsConstructor
@Table("user_allocation")
public class UserAllocation extends BaseEntity {
    private Long companyId;

    private Long packageId;

    private String userId;

    private Integer allocatedUnits = 0;

    private Integer consumedUnits = 0;

    private AllocationStatus status = AllocationStatus.ACTIVE;

    private String contextRef;

    private LocalDateTime createdAt;

    private String createdBy;

    private LocalDateTime updatedAt;

    private String updatedBy;

    public enum AllocationStatus {
        ACTIVE,
        INACTIVE
    }
}
