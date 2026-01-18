/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.session.user_allocation;

import com.topleader.topleader.common.exception.ApiValidationException;
import com.topleader.topleader.common.exception.NotFoundException;
import com.topleader.topleader.session.coaching_package.CoachingPackage;
import com.topleader.topleader.session.coaching_package.CoachingPackageRepository;
import com.topleader.topleader.session.user_allocation.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.topleader.topleader.common.exception.ErrorCodeConstants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAllocationService {


    private final UserAllocationRepository userAllocationRepository;
    private final CoachingPackageRepository coachingPackageRepository;

    public UserAllocationDto createAllocation(Long packageId, String userId, AllocationRequest request, String createdBy) {
        log.info("Creating allocation for package {} user {} by {}", packageId, userId, createdBy);

        var pkg = getActivePackage(packageId);
        var existingAllocation = userAllocationRepository.findByPackageIdAndUserId(packageId, userId);

        if (existingAllocation.isPresent()) {
            throw new ApiValidationException(ALLOCATION_ALREADY_EXISTS, "userId", userId,
                    "Allocation already exists for user " + userId + " in package " + packageId);
        }

        validateCapacity(pkg, request.allocatedUnits());

        var now = LocalDateTime.now();
        var allocation = new UserAllocation()
                .setPackageId(packageId)
                .setCompanyId(pkg.getCompanyId())
                .setUserId(userId)
                .setAllocatedUnits(request.allocatedUnits())
                .setStatus(request.status() != null ? request.status() : UserAllocation.AllocationStatus.ACTIVE)
                .setContextRef(request.contextRef())
                .setCreatedAt(now)
                .setCreatedBy(createdBy)
                .setUpdatedAt(now)
                .setUpdatedBy(createdBy);

        var saved = userAllocationRepository.save(allocation);
        log.info("Created allocation {} for package {} user {}", saved.getId(), packageId, userId);

        return UserAllocationDto.from(saved);
    }

    public UserAllocationDto updateAllocation(Long packageId, String userId, AllocationRequest request, String updatedBy) {
        log.info("Updating allocation for package {} user {} by {}", packageId, userId, updatedBy);

        var pkg = getActivePackage(packageId);
        var allocation = userAllocationRepository.findByPackageIdAndUserIdForUpdate(packageId, userId)
                .orElseThrow(NotFoundException::new);

        int currentAllocated = allocation.getAllocatedUnits();
        int currentConsumed = allocation.getConsumedUnits();
        int delta = request.allocatedUnits() - currentAllocated;

        // Validate: allocatedUnits >= consumedUnits
        if (request.allocatedUnits() < currentConsumed) {
            throw new ApiValidationException(ALLOCATED_BELOW_CONSUMED, "allocatedUnits",
                    String.valueOf(request.allocatedUnits()),
                    "Allocated units cannot be less than consumed units (" + currentConsumed + ")");
        }

        validateCapacity(pkg, delta);

        allocation.setAllocatedUnits(request.allocatedUnits())
                .setStatus(request.status() != null ? request.status() : allocation.getStatus())
                .setContextRef(request.contextRef())
                .setUpdatedAt(LocalDateTime.now())
                .setUpdatedBy(updatedBy);

        var saved = userAllocationRepository.save(allocation);
        log.info("Updated allocation {} for package {} user {}", saved.getId(), packageId, userId);

        return UserAllocationDto.from(saved);
    }

    public BulkAllocationResponse bulkAllocate(Long packageId, BulkAllocationRequest request, String updatedBy) {
        log.info("Bulk allocating {} items for package {} by {}", request.items().size(), packageId, updatedBy);

        var pkg = getActivePackage(packageId);

        var totalDelta = request.items().stream()
                .mapToInt(item -> {
                    var existing = userAllocationRepository.findByPackageIdAndUserId(packageId, item.userId());
                    var currentAllocated = existing.map(UserAllocation::getAllocatedUnits).orElse(0);
                    var currentConsumed = existing.map(UserAllocation::getConsumedUnits).orElse(0);

                    if (item.allocatedUnits() < currentConsumed) {
                        throw new ApiValidationException(ALLOCATED_BELOW_CONSUMED, "allocatedUnits",
                                item.userId() + ":" + item.allocatedUnits(),
                                "Allocated units for user " + item.userId() + " cannot be less than consumed units (" + currentConsumed + ")");
                    }

                    return item.allocatedUnits() - currentAllocated;
                })
                .sum();

        validateCapacity(pkg, totalDelta);

        var results = request.items().stream()
                .map(item -> {
                    var existing = userAllocationRepository.findByPackageIdAndUserIdForUpdate(packageId, item.userId());
                    var now = LocalDateTime.now();

                    var allocation = existing.orElseGet(() -> new UserAllocation()
                            .setPackageId(packageId)
                            .setCompanyId(pkg.getCompanyId())
                            .setUserId(item.userId())
                            .setCreatedAt(now)
                            .setCreatedBy(updatedBy));

                    allocation.setAllocatedUnits(item.allocatedUnits())
                            .setStatus(UserAllocation.AllocationStatus.ACTIVE)
                            .setUpdatedAt(now)
                            .setUpdatedBy(updatedBy);

                    var saved = userAllocationRepository.save(allocation);
                    return new BulkAllocationResponse.BulkAllocationItemResponse(
                            saved.getUserId(),
                            saved.getAllocatedUnits(),
                            saved.getStatus()
                    );
                })
                .toList();

        log.info("Bulk allocated {} items for package {}", results.size(), packageId);
        return new BulkAllocationResponse(results.size(), results);
    }

    public UserAllocation consumeUnit(Long packageId, String userId) {
        log.info("Consuming unit for package {} user {}", packageId, userId);

        var allocation = userAllocationRepository.findByPackageIdAndUserIdForUpdate(packageId, userId)
                .orElseThrow(NotFoundException::new);

        if (allocation.getStatus() != UserAllocation.AllocationStatus.ACTIVE) {
            throw new ApiValidationException(PACKAGE_INACTIVE, "allocationId", allocation.getId().toString(),
                    "Allocation is not active");
        }

        if (allocation.getConsumedUnits() >= allocation.getAllocatedUnits()) {
            throw new ApiValidationException(NO_UNITS_AVAILABLE, "userId", userId,
                    "No available units to consume. Allocated: " + allocation.getAllocatedUnits() +
                    ", Consumed: " + allocation.getConsumedUnits());
        }

        allocation.setConsumedUnits(allocation.getConsumedUnits() + 1)
                .setUpdatedAt(LocalDateTime.now());
        var saved = userAllocationRepository.save(allocation);

        log.info("Consumed unit for allocation {}. Consumed: {}/{}",
                saved.getId(), saved.getConsumedUnits(), saved.getAllocatedUnits());

        return saved;
    }

    public int getAvailableUnits(Long packageId, String userId) {
        return userAllocationRepository.findByPackageIdAndUserId(packageId, userId)
                .filter(a -> a.getStatus() == UserAllocation.AllocationStatus.ACTIVE)
                .map(a -> a.getAllocatedUnits() - a.getConsumedUnits())
                .orElse(0);
    }

    public List<UserAllocationDto> listAllocations(Long packageId) {
        log.debug("Listing allocations for package {}", packageId);
        return userAllocationRepository.findByPackageId(packageId).stream()
                .map(UserAllocationDto::from)
                .toList();
    }

    public UserAllocation getAllocationEntity(Long allocationId) {
        return userAllocationRepository.findById(allocationId)
                .orElseThrow(NotFoundException::new);
    }

    public UserAllocationDto updateAllocationStatus(Long allocationId, UpdateAllocationStatusRequest request, String updatedBy) {
        log.info("Updating allocation {} status to {} by {}", allocationId, request.status(), updatedBy);

        var allocation = userAllocationRepository.findById(allocationId)
                .orElseThrow(NotFoundException::new);

        allocation.setStatus(request.status())
                .setUpdatedAt(LocalDateTime.now())
                .setUpdatedBy(updatedBy);

        // When setting to INACTIVE, set allocated to consumed (release unused units)
        if (request.status() == UserAllocation.AllocationStatus.INACTIVE) {
            allocation.setAllocatedUnits(allocation.getConsumedUnits());
        }

        var saved = userAllocationRepository.save(allocation);
        return UserAllocationDto.from(saved);
    }


    private CoachingPackage getActivePackage(Long packageId) {
        var pkg = coachingPackageRepository.findById(packageId)
                .orElseThrow(NotFoundException::new);

        if (pkg.getStatus() != CoachingPackage.PackageStatus.ACTIVE) {
            throw new ApiValidationException(PACKAGE_INACTIVE, "packageId", packageId.toString(),
                    "Package is not active");
        }

        return pkg;
    }

    private void validateCapacity(CoachingPackage pkg, int delta) {
        if (delta <= 0) {
            return; // Decreasing or no change is always allowed
        }

        // Sum ACTIVE allocations (allocatedUnits) + INACTIVE allocations (consumedUnits)
        int currentUsed = userAllocationRepository.sumUsedUnitsByPackageId(pkg.getId());
        int availableUnits = pkg.getTotalUnits() - currentUsed;

        if (delta > availableUnits) {
            throw new ApiValidationException(CAPACITY_EXCEEDED, "allocatedUnits", String.valueOf(delta),
                    "Allocation exceeds package capacity. Available units: " + availableUnits);
        }
    }
}
