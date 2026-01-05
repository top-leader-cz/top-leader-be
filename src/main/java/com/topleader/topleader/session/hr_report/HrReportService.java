/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.session.hr_report;

import com.topleader.topleader.common.exception.NotFoundException;
import com.topleader.topleader.session.coaching_package.CoachingPackage;
import com.topleader.topleader.session.coaching_package.CoachingPackageRepository;
import com.topleader.topleader.session.scheduled_session.ScheduledSessionRepository;
import com.topleader.topleader.session.user_allocation.UserAllocation;
import com.topleader.topleader.session.user_allocation.UserAllocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class HrReportService {

    private final CoachingPackageRepository coachingPackageRepository;
    private final UserAllocationRepository userAllocationRepository;
    private final ScheduledSessionRepository scheduledSessionRepository;

    @Transactional(readOnly = true)
    public HrReportResponse generateReport(Long packageId, LocalDate from, LocalDate to) {
        log.debug("Generating HR report for package {} (from={}, to={})", packageId, from, to);

        var pkg = coachingPackageRepository.findById(packageId)
                .orElseThrow(NotFoundException::new);

        var fromDateTime = from != null ? from.atStartOfDay() : LocalDateTime.of(1970, 1, 1, 0, 0);
        var toDateTime = to != null ? to.plusDays(1).atStartOfDay() : LocalDateTime.of(2100, 1, 1, 0, 0);

        var packageInfo = HrReportResponse.PackageInfo.from(pkg);
        var summary = computeSummary(pkg, fromDateTime, toDateTime);
        var rows = computeUserRows(packageId, fromDateTime, toDateTime);

        return new HrReportResponse(packageInfo, summary, rows);
    }

    private HrReportResponse.Summary computeSummary(CoachingPackage pkg, LocalDateTime from, LocalDateTime to) {
        var packageId = pkg.getId();
        var totalUnits = pkg.getTotalUnits();
        var allocatedUnits = userAllocationRepository.sumAllocatedUnitsByPackageId(packageId);

        var userIds = userAllocationRepository.findByPackageId(packageId).stream()
                .map(UserAllocation::getUserId)
                .toList();

        var plannedSessions = userIds.isEmpty() ? 0 : scheduledSessionRepository.countUpcomingByUsernamesAndTimeRange(userIds, from, to);
        var completedSessions = userIds.isEmpty() ? 0 : scheduledSessionRepository.countCompletedByUsernamesAndTimeRange(userIds, from, to);
        var noShowClientSessions = userIds.isEmpty() ? 0 : scheduledSessionRepository.countNoShowClientByUsernamesAndTimeRange(userIds, from, to);

        return HrReportResponse.Summary.of(totalUnits, allocatedUnits, plannedSessions, completedSessions, noShowClientSessions);
    }

    private java.util.List<HrReportResponse.UserRow> computeUserRows(Long packageId, LocalDateTime from, LocalDateTime to) {
        var allocations = userAllocationRepository.findByPackageId(packageId);

        return allocations.stream()
                .map(allocation -> {
                    var userId = allocation.getUserId();
                    var allocatedUnits = allocation.getAllocatedUnits();
                    var plannedSessions = scheduledSessionRepository.countUpcomingByUsernameAndTimeRange(userId, from, to);
                    var completedSessions = scheduledSessionRepository.countCompletedByUsernameAndTimeRange(userId, from, to);
                    var noShowClientSessions = scheduledSessionRepository.countNoShowClientByUsernameAndTimeRange(userId, from, to);

                    return HrReportResponse.UserRow.of(userId, allocatedUnits, plannedSessions, completedSessions, noShowClientSessions);
                })
                .toList();
    }
}
