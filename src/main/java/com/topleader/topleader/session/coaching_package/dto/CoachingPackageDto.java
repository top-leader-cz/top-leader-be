package com.topleader.topleader.session.coaching_package.dto;


import com.topleader.topleader.session.coaching_package.CoachingPackage;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public record CoachingPackageDto(
        Long id,
        Long companyId,
        CoachingPackage.PoolType poolType,
        int totalUnits,
        ZonedDateTime validFrom,
        ZonedDateTime validTo,
        CoachingPackage.PackageStatus status,
        String contextRef,
        ZonedDateTime createdAt,
        String createdBy,
        ZonedDateTime updatedAt,
        String updatedBy,
        CoachingPackageMetricsDto metrics
) {

    public static CoachingPackageDto from(CoachingPackage entity, CoachingPackageMetricsDto metrics) {
        return new CoachingPackageDto(
                entity.getId(),
                entity.getCompanyId(),
                entity.getPoolType(),
                entity.getTotalUnits(),
                toUtc(entity.getValidFrom()),
                toUtc(entity.getValidTo()),
                entity.getStatus(),
                entity.getContextRef(),
                toUtc(entity.getCreatedAt()),
                entity.getCreatedBy(),
                toUtc(entity.getUpdatedAt()),
                entity.getUpdatedBy(),
                metrics
        );
    }

    private static ZonedDateTime toUtc(LocalDateTime ldt) {
        return ldt == null ? null : ldt.atZone(ZoneOffset.UTC);
    }
}
