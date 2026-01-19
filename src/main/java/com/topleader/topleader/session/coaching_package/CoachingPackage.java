/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.session.coaching_package;

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
@Table("coaching_package")
public class CoachingPackage extends BaseEntity {
    private Long companyId;

    private PoolType poolType;

    private Integer totalUnits;

    private LocalDateTime validFrom;

    private LocalDateTime validTo;

    private PackageStatus status = PackageStatus.ACTIVE;

    private String contextRef;

    private LocalDateTime createdAt;

    private String createdBy;

    private LocalDateTime updatedAt;

    private String updatedBy;

    public enum PoolType {
        CORE,
        MASTER
    }

    public enum PackageStatus {
        ACTIVE,
        INACTIVE
    }
}
