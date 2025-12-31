/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.session.hr_report;

import com.topleader.topleader.session.coaching_package.CoachingPackage;

import java.time.LocalDateTime;
import java.util.List;

public record HrReportResponse(
        PackageInfo packageInfo,
        Summary summary,
        List<UserRow> rows
) {

    public record PackageInfo(
            Long id,
            Long companyId,
            CoachingPackage.PoolType poolType,
            LocalDateTime validFrom,
            LocalDateTime validTo,
            CoachingPackage.PackageStatus status
    ) {
        public static PackageInfo from(CoachingPackage pkg) {
            return new PackageInfo(
                    pkg.getId(),
                    pkg.getCompanyId(),
                    pkg.getPoolType(),
                    pkg.getValidFrom(),
                    pkg.getValidTo(),
                    pkg.getStatus()
            );
        }
    }

    public record Summary(
            int totalUnits,
            int allocatedUnits,
            int plannedSessions,
            int completedSessions,
            int reservedUnits,
            int consumedUnits,
            int remainingUnits,
            int unallocatedUnits
    ) {
        public static Summary of(int totalUnits, int allocatedUnits, int plannedSessions, int completedSessions) {
            var reservedUnits = plannedSessions;
            var consumedUnits = completedSessions;
            var remainingUnits = totalUnits - reservedUnits - consumedUnits;
            var unallocatedUnits = totalUnits - allocatedUnits - reservedUnits - consumedUnits;
            return new Summary(
                    totalUnits,
                    allocatedUnits,
                    plannedSessions,
                    completedSessions,
                    reservedUnits,
                    consumedUnits,
                    Math.max(0, remainingUnits),
                    Math.max(0, unallocatedUnits)
            );
        }
    }

    public record UserRow(
            String userId,
            int allocatedUnits,
            int plannedSessions,
            int completedSessions,
            int reservedUnits,
            int consumedUnits,
            int remainingUnits,
            boolean dataIssue
    ) {
        public static UserRow of(String userId, int allocatedUnits, int plannedSessions, int completedSessions) {
            var reservedUnits = plannedSessions;
            var consumedUnits = completedSessions;
            var remainingUnits = allocatedUnits - reservedUnits - consumedUnits;
            var dataIssue = remainingUnits < 0;
            return new UserRow(
                    userId,
                    allocatedUnits,
                    plannedSessions,
                    completedSessions,
                    reservedUnits,
                    consumedUnits,
                    Math.max(0, remainingUnits),
                    dataIssue
            );
        }
    }
}
