/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.session.coaching_package;


import com.topleader.topleader.company.CompanyRepository;
import com.topleader.topleader.exception.NotFoundException;
import com.topleader.topleader.session.coaching_package.dto.CoachingPackageDto;
import com.topleader.topleader.session.coaching_package.dto.CoachingPackageMetricsDto;
import com.topleader.topleader.session.coaching_package.dto.CreateCoachingPackageRequest;
import com.topleader.topleader.session.coaching_package.dto.UpdateCoachingPackageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * Service for managing coaching packages.
 *
 * Metrics calculation is centralized here to ensure consistency across all endpoints.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CoachingPackageService {

    private final CoachingPackageRepository coachingPackageRepository;
    private final CompanyRepository companyRepository;

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
                .setUpdatedBy(updatedBy);

        var saved = coachingPackageRepository.save(entity);
        log.info("Updated coaching package {} status to {}", packageId, request.status());

        return toDto(saved);
    }


    private CoachingPackageDto toDto(CoachingPackage entity) {
        var metrics = computeMetrics(entity);
        return CoachingPackageDto.from(entity, metrics);
    }

    /**
     * Computes metrics for a coaching package.
     *
     * In V1 (Epic 1):
     * - allocated_units: 0 (no user allocation table yet, will be sum of active allocations later)
     * - reserved_units: 0 (no booking logic yet)
     * - consumed_units: 0 (no booking logic yet)
     *
     * @param entity the coaching package entity
     * @return computed metrics
     */
    private CoachingPackageMetricsDto computeMetrics(CoachingPackage entity) {
        int totalUnits = entity.getTotalUnits();

        // V1: These will be computed from related tables in future epics
        int allocatedUnits = computeAllocatedUnits(entity.getId());
        int reservedUnits = computeReservedUnits(entity.getId());
        int consumedUnits = computeConsumedUnits(entity.getId());

        return CoachingPackageMetricsDto.of(totalUnits, allocatedUnits, reservedUnits, consumedUnits);
    }

    /**
     * Computes allocated units for a package.
     *
     * V1: Returns 0 (no user allocation table yet).
     * Future: Will query user_allocation table for sum of active allocations.
     *
     * @param packageId the package ID
     * @return allocated units
     */
    private int computeAllocatedUnits(Long packageId) {
        // TODO: In Epic 2, query user_allocation table:
        // return userAllocationRepository.sumActiveAllocationsByPackageId(packageId);
        return 0;
    }

    /**
     * Computes reserved units for a package.
     *
     * V1: Returns 0 (no booking logic yet).
     * Future: Will query bookings table for scheduled bookings.
     *
     * @param packageId the package ID
     * @return reserved units
     */
    private int computeReservedUnits(Long packageId) {
        // TODO: In Epic 2, query bookings table:
        // return bookingRepository.sumReservedUnitsByPackageId(packageId);
        return 0;
    }

    /**
     * Computes consumed units for a package.
     *
     * V1: Returns 0 (no booking logic yet).
     * Future: Will query bookings table for completed/no-show bookings.
     *
     * @param packageId the package ID
     * @return consumed units
     */
    private int computeConsumedUnits(Long packageId) {
        // TODO: In Epic 2, query bookings table:
        // return bookingRepository.sumConsumedUnitsByPackageId(packageId);
        return 0;
    }
}
