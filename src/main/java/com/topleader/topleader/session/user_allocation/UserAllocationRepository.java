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

    Optional<UserAllocation> findByPackageIdAndUserId(Long packageId, String userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ua FROM UserAllocation ua WHERE ua.packageId = :packageId AND ua.userId = :userId")
    Optional<UserAllocation> findByPackageIdAndUserIdForUpdate(Long packageId, String userId);

    @Query("SELECT COALESCE(SUM(ua.allocatedUnits), 0) FROM UserAllocation ua WHERE ua.packageId = :packageId")
    int sumAllocatedUnitsByPackageId(Long packageId);

    @Query("SELECT COALESCE(SUM(ua.allocatedUnits), 0) FROM UserAllocation ua WHERE ua.packageId = :packageId")
    int sumUsedUnitsByPackageId(Long packageId);

}
