/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.hr.hr_report;

import com.topleader.topleader.session.coaching_package.CoachingPackage;
import com.topleader.topleader.user.User;

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
            int consumedUnits,
            int remainingUnits,
            int unallocatedUnits,
            int noShowClientSessions
    ) {
        public static Summary of(int totalUnits, int allocatedUnits, int plannedSessions, int completedSessions, int noShowClientSessions) {
            var consumedUnits = completedSessions + noShowClientSessions;
            var remainingUnits = totalUnits - plannedSessions - consumedUnits;
            var unallocatedUnits = totalUnits - allocatedUnits;
            return new Summary(
                    totalUnits,
                    allocatedUnits,
                    plannedSessions,
                    completedSessions,
                    consumedUnits,
                    Math.max(0, remainingUnits),
                    Math.max(0, unallocatedUnits),
                    noShowClientSessions
            );
        }
    }

    public record UserRow(
            String userId,
            String firstName,
            String lastName,
            int allocatedUnits,
            int plannedSessions,
            int completedSessions,
            int noShowClientSessions,
            int consumedUnits,
            int remainingUnits,
            boolean dataIssue
    ) {
        public static UserRow of(User user, int allocatedUnits, int plannedSessions, int completedSessions, int noShowClientSessions) {
            var consumedUnits = completedSessions + noShowClientSessions;
            var remainingUnits = allocatedUnits - plannedSessions - consumedUnits;
            var dataIssue = remainingUnits < 0;
            return new UserRow(
                    user.getUsername(),
                    user.getFirstName(),
                    user.getLastName(),
                    allocatedUnits,
                    plannedSessions,
                    completedSessions,
                    noShowClientSessions,
                    consumedUnits,
                    Math.max(0, remainingUnits),
                    dataIssue
            );
        }
    }
}
