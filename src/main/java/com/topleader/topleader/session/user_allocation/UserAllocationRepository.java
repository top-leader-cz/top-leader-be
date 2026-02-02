package com.topleader.topleader.session.user_allocation;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.Optional;

public interface UserAllocationRepository extends ListCrudRepository<UserAllocation, Long> {

    List<UserAllocation> findByPackageId(Long packageId);

    Optional<UserAllocation> findByPackageIdAndUsername(Long packageId, String username);

    @Query("SELECT * FROM user_allocation WHERE package_id = :packageId AND username = :username FOR UPDATE")
    Optional<UserAllocation> findByPackageIdAndUsernameForUpdate(Long packageId, String username);

    @Query("SELECT COALESCE(SUM(allocated_units), 0) FROM user_allocation WHERE package_id = :packageId")
    int sumAllocatedUnitsByPackageId(Long packageId);

    @Query("SELECT COALESCE(SUM(allocated_units), 0) FROM user_allocation WHERE package_id = :packageId")
    int sumUsedUnitsByPackageId(Long packageId);

    @Query("""
        SELECT ua.* FROM user_allocation ua
        JOIN coaching_package cp ON ua.package_id = cp.id
        WHERE ua.username = :username
          AND ua.status = 'ACTIVE'
          AND cp.status = 'ACTIVE'
        """)
    List<UserAllocation> findActiveByUsername(String username);

}
