/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.session.coaching_package;



import com.topleader.topleader.common.exception.NotFoundException;
import com.topleader.topleader.hr.company.CompanyRepository;
import com.topleader.topleader.session.coaching_package.dto.CoachingPackageDto;
import com.topleader.topleader.session.coaching_package.dto.CoachingPackageMetricsDto;
import com.topleader.topleader.session.coaching_package.dto.CreateCoachingPackageRequest;
import com.topleader.topleader.session.coaching_package.dto.UpdateCoachingPackageRequest;
import com.topleader.topleader.session.scheduled_session.ScheduledSessionRepository;
import com.topleader.topleader.session.user_allocation.UserAllocation;
import com.topleader.topleader.session.user_allocation.UserAllocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoachingPackageService {

    private final CoachingPackageRepository coachingPackageRepository;
    private final CompanyRepository companyRepository;
    private final UserAllocationRepository userAllocationRepository;
    private final ScheduledSessionRepository scheduledSessionRepository;

    /**
     * Creates a new coaching package for a company.
     *
     * @param companyId the company ID
     * @param request the create request
     * @param createdBy the username of the creator
     * @return the created package with metrics
     */
    @Transactional
    public CoachingPackageDto createPackage(Long companyId, CreateCoachingPackageRequest request, String createdBy) {
        log.info("Creating coaching package for company {} by user {}", companyId, createdBy);

        // Verify company exists
        companyRepository.findById(companyId)
                .orElseThrow(NotFoundException::new);

        var entity = new CoachingPackage()
                .setCompanyId(companyId)
                .setPoolType(request.poolType())
                .setTotalUnits(request.totalUnits())
                .setValidFrom(request.validFrom())
                .setValidTo(request.validTo())
                .setContextRef(request.contextRef())
                .setCreatedBy(createdBy)
                .setUpdatedBy(createdBy);

        var saved = coachingPackageRepository.save(entity);
        log.info("Created coaching package {} for company {}", saved.getId(), companyId);

        return toDto(saved);
    }

    /**
     * Lists all coaching packages for a company with metrics.
     *
     * @param companyId the company ID
     * @return list of packages with metrics
     */
    @Transactional(readOnly = true)
    public List<CoachingPackageDto> listPackagesByCompany(Long companyId) {
        log.debug("Listing coaching packages for company {}", companyId);

        return coachingPackageRepository.findByCompanyId(companyId).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Gets a single coaching package by ID with metrics.
     *
     * @param packageId the package ID
     * @return the package with metrics
     */
    @Transactional(readOnly = true)
    public CoachingPackageDto getPackage(Long packageId) {
        log.debug("Getting coaching package {}", packageId);

        CoachingPackage entity = coachingPackageRepository.findById(packageId)
                .orElseThrow(NotFoundException::new);

        return toDto(entity);
    }


    @Transactional(readOnly = true)
    public CoachingPackage getPackageEntity(Long packageId) {
        return coachingPackageRepository.findById(packageId)
                .orElseThrow(NotFoundException::new);
    }


    @Transactional
    public CoachingPackageDto updatePackage(Long packageId, UpdateCoachingPackageRequest request, String updatedBy) {
        log.info("Updating coaching package {} by user {}", packageId, updatedBy);

        var entity = coachingPackageRepository.findById(packageId)
                .orElseThrow(NotFoundException::new);

        entity.setStatus(request.status())
                        .setUpdatedBy(updatedBy)
                        .setTotalUnits(request.totalUnits() > entity.getTotalUnits() ? request.totalUnits(): entity.getTotalUnits());

        var saved = coachingPackageRepository.save(entity);
        log.info("Updated coaching package {}", packageId);

        return toDto(saved);
    }


    private CoachingPackageDto toDto(CoachingPackage entity) {
        var metrics = computeMetrics(entity);
        return CoachingPackageDto.from(entity, metrics);
    }

    private CoachingPackageMetricsDto computeMetrics(CoachingPackage entity) {
        int totalUnits = entity.getTotalUnits();
        int allocatedUnits = computeAllocatedUnits(entity.getId());
        int reservedUnits = computeReservedUnits(entity.getId());
        int consumedUnits = computeConsumedUnits(entity.getId());

        return CoachingPackageMetricsDto.of(totalUnits, allocatedUnits, reservedUnits, consumedUnits);
    }

    private int computeAllocatedUnits(Long packageId) {
        return userAllocationRepository.sumAllocatedUnitsByPackageId(packageId);
    }

    private int computeReservedUnits(Long packageId) {
        var userIds = getUserIdsForPackage(packageId);
        return userIds.isEmpty() ? 0 : scheduledSessionRepository.countUpcomingByUsernames(userIds);
    }

    private int computeConsumedUnits(Long packageId) {
        var userIds = getUserIdsForPackage(packageId);
        if (userIds.isEmpty()) {
            return 0;
        }
        return scheduledSessionRepository.countConsumedByUsernames(userIds);
    }

    private List<String> getUserIdsForPackage(Long packageId) {
        return userAllocationRepository.findByPackageId(packageId).stream()
                .map(UserAllocation::getUserId)
                .toList();
    }
}
