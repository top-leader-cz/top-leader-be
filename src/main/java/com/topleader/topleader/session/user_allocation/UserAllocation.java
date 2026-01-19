/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.session.user_allocation;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@Table("user_allocation")
public class UserAllocation {

    @Id
    private Long id;

    private Long companyId;

    private Long packageId;

    private String username;

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
