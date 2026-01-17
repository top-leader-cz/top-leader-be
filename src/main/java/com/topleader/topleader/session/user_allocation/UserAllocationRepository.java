/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.session.user_allocation;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Optional;

public interface UserAllocationRepository extends CrudRepository<UserAllocation, Long>, PagingAndSortingRepository<UserAllocation, Long> {

    List<UserAllocation> findByPackageId(Long packageId);

    Optional<UserAllocation> findByPackageIdAndUserId(Long packageId, String userId);

    @Query("SELECT * FROM user_allocation WHERE package_id = :packageId AND user_id = :userId FOR UPDATE")
    Optional<UserAllocation> findByPackageIdAndUserIdForUpdate(Long packageId, String userId);

    @Query("SELECT COALESCE(SUM(allocated_units), 0) FROM user_allocation WHERE package_id = :packageId")
    int sumAllocatedUnitsByPackageId(Long packageId);

    @Query("SELECT COALESCE(SUM(allocated_units), 0) FROM user_allocation WHERE package_id = :packageId")
    int sumUsedUnitsByPackageId(Long packageId);

}
