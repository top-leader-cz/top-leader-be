/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.session.coaching_package.dto;


import com.topleader.topleader.session.coaching_package.CoachingPackage;

import java.time.LocalDateTime;

public record CoachingPackageDto(
        Long id,
        Long companyId,
        CoachingPackage.PoolType poolType,
        int totalUnits,
        LocalDateTime validFrom,
        LocalDateTime validTo,
        CoachingPackage.PackageStatus status,
        String contextRef,
        LocalDateTime createdAt,
        String createdBy,
        LocalDateTime updatedAt,
        String updatedBy,
        CoachingPackageMetricsDto metrics
) {

    public static CoachingPackageDto from(CoachingPackage entity, CoachingPackageMetricsDto metrics) {
        return new CoachingPackageDto(
                entity.getId(),
                entity.getCompanyId(),
                entity.getPoolType(),
                entity.getTotalUnits(),
                entity.getValidFrom(),
                entity.getValidTo(),
                entity.getStatus(),
                entity.getContextRef(),
                entity.getCreatedAt(),
                entity.getCreatedBy(),
                entity.getUpdatedAt(),
                entity.getUpdatedBy(),
                metrics
        );
    }
}
