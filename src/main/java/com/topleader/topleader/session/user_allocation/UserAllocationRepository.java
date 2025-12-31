/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.session.user_allocation;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserAllocationRepository extends JpaRepository<UserAllocation, Long> {

    List<UserAllocation> findByPackageId(Long packageId);

    List<UserAllocation> findByPackageIdAndStatus(Long packageId, UserAllocation.AllocationStatus status);

    Optional<UserAllocation> findByPackageIdAndUserId(Long packageId, String userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ua FROM UserAllocation ua WHERE ua.packageId = :packageId AND ua.userId = :userId")
    Optional<UserAllocation> findByPackageIdAndUserIdForUpdate(Long packageId, String userId);

    @Query("SELECT COALESCE(SUM(ua.allocatedUnits), 0) FROM UserAllocation ua WHERE ua.packageId = :packageId AND ua.status = 'ACTIVE'")
    int sumAllocatedUnitsByPackageId(Long packageId);

    List<UserAllocation> findByUserId(String userId);

    List<UserAllocation> findByUserIdAndStatus(String userId, UserAllocation.AllocationStatus status);
}
