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

    public UserAllocationDto saveAllocation(Long packageId, String userId, AllocationRequest request, String savedBy) {
        log.info("Saving allocation for package {} user {} by {}", packageId, userId, savedBy);

        var pkg = getActivePackage(packageId);
        var now = LocalDateTime.now();

        return userAllocationRepository.findByPackageIdAndUsernameForUpdate(packageId, userId)
                .map(allocation -> updateAllocation(allocation, pkg, request, savedBy, now))
                .orElseGet(() -> createAllocation(pkg, userId, request, savedBy, now));
    }

    private UserAllocationDto updateAllocation(UserAllocation allocation, CoachingPackage pkg,
                                               AllocationRequest request, String updatedBy, LocalDateTime now) {
        int delta = request.allocatedUnits() - allocation.getAllocatedUnits();

        if (request.allocatedUnits() < allocation.getConsumedUnits()) {
            throw new ApiValidationException(ALLOCATED_BELOW_CONSUMED, "allocatedUnits",
                    String.valueOf(request.allocatedUnits()),
                    "Allocated units cannot be less than consumed units (" + allocation.getConsumedUnits() + ")");
        }

        validateCapacity(pkg, delta);

        allocation.setAllocatedUnits(request.allocatedUnits())
                .setStatus(request.status() != null ? request.status() : allocation.getStatus())
                .setContextRef(request.contextRef())
                .setUpdatedAt(now)
                .setUpdatedBy(updatedBy);

        var saved = userAllocationRepository.save(allocation);
        log.info("Updated allocation {} for package {} user {}", saved.getId(), pkg.getId(), allocation.getUsername());

        return UserAllocationDto.from(saved);
    }

    private UserAllocationDto createAllocation(CoachingPackage pkg, String userId,
                                               AllocationRequest request, String createdBy, LocalDateTime now) {
        validateCapacity(pkg, request.allocatedUnits());

        var allocation = new UserAllocation()
                .setPackageId(pkg.getId())
                .setCompanyId(pkg.getCompanyId())
                .setUsername(userId)
                .setAllocatedUnits(request.allocatedUnits())
                .setStatus(request.status() != null ? request.status() : UserAllocation.AllocationStatus.ACTIVE)
                .setContextRef(request.contextRef())
                .setCreatedAt(now)
                .setCreatedBy(createdBy)
                .setUpdatedAt(now)
                .setUpdatedBy(createdBy);

        var saved = userAllocationRepository.save(allocation);
        log.info("Created allocation {} for package {} user {}", saved.getId(), pkg.getId(), userId);

        return UserAllocationDto.from(saved);
    }

    public BulkAllocationResponse bulkAllocate(Long packageId, BulkAllocationRequest request, String updatedBy) {
        log.info("Bulk allocating {} items for package {} by {}", request.items().size(), packageId, updatedBy);

        var pkg = getActivePackage(packageId);

        var totalDelta = request.items().stream()
                .mapToInt(item -> {
                    var existing = userAllocationRepository.findByPackageIdAndUsername(packageId, item.username());
                    var currentAllocated = existing.map(UserAllocation::getAllocatedUnits).orElse(0);
                    var currentConsumed = existing.map(UserAllocation::getConsumedUnits).orElse(0);

                    if (item.allocatedUnits() < currentConsumed) {
                        throw new ApiValidationException(ALLOCATED_BELOW_CONSUMED, "allocatedUnits",
                                item.username() + ":" + item.allocatedUnits(),
                                "Allocated units for user " + item.username() + " cannot be less than consumed units (" + currentConsumed + ")");
                    }

                    return item.allocatedUnits() - currentAllocated;
                })
                .sum();

        validateCapacity(pkg, totalDelta);

        var results = request.items().stream()
                .map(item -> {
                    var existing = userAllocationRepository.findByPackageIdAndUsernameForUpdate(packageId, item.username());
                    var now = LocalDateTime.now();

                    var allocation = existing.orElseGet(() -> new UserAllocation()
                            .setPackageId(packageId)
                            .setCompanyId(pkg.getCompanyId())
                            .setUsername(item.username())
                            .setCreatedAt(now)
                            .setCreatedBy(updatedBy));

                    allocation.setAllocatedUnits(item.allocatedUnits())
                            .setStatus(UserAllocation.AllocationStatus.ACTIVE)
                            .setUpdatedAt(now)
                            .setUpdatedBy(updatedBy);

                    var saved = userAllocationRepository.save(allocation);
                    return new BulkAllocationResponse.BulkAllocationItemResponse(
                            saved.getUsername(),
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

        var allocation = userAllocationRepository.findByPackageIdAndUsernameForUpdate(packageId, userId)
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

    public void consumeUnit(String username) {
        log.info("Consuming unit for user {}", username);

        var allocation = userAllocationRepository.findActiveByUsername(username).stream()
                .filter(a -> a.getAllocatedUnits() > a.getConsumedUnits())
                .findFirst()
                .orElseThrow(() -> new ApiValidationException(NO_UNITS_AVAILABLE, "username", username,
                        "No available units to consume"));

        allocation.setConsumedUnits(allocation.getConsumedUnits() + 1)
                .setUpdatedAt(LocalDateTime.now());
        var saved = userAllocationRepository.save(allocation);

        log.info("Consumed unit for allocation {}. Consumed: {}/{}",
                saved.getId(), saved.getConsumedUnits(), saved.getAllocatedUnits());
    }

    public void releaseUnit(String username) {
        log.info("Releasing unit for user {}", username);

        var allocation = userAllocationRepository.findActiveByUsername(username).stream()
                .filter(a -> a.getConsumedUnits() > 0)
                .findFirst()
                .orElse(null);

        if (allocation == null) {
            log.warn("No allocation with consumed units found for user {}", username);
            return;
        }

        allocation.setConsumedUnits(allocation.getConsumedUnits() - 1)
                .setUpdatedAt(LocalDateTime.now());
        var saved = userAllocationRepository.save(allocation);

        log.info("Released unit for allocation {}. Consumed: {}/{}",
                saved.getId(), saved.getConsumedUnits(), saved.getAllocatedUnits());
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
