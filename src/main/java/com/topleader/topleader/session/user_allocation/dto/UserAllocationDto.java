/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.session.user_allocation.dto;

import com.topleader.topleader.session.user_allocation.UserAllocation;

public record UserAllocationDto(
        Long id,
        Long companyId,
        Long packageId,
        String userId,
        int allocatedUnits,
        int consumedUnits,
        int reservedUnits,
        int remainingUnits,
        UserAllocation.AllocationStatus status,
        String contextRef
) {

    public static UserAllocationDto from(UserAllocation entity) {
        int reserved = 0; // V1: no booking logic yet
        int remaining = entity.getAllocatedUnits() - entity.getConsumedUnits() - reserved;
        return new UserAllocationDto(
                entity.getId(),
                entity.getCompanyId(),
                entity.getPackageId(),
                entity.getUserId(),
                entity.getAllocatedUnits(),
                entity.getConsumedUnits(),
                reserved,
                remaining,
                entity.getStatus(),
                entity.getContextRef()
        );
    }
}
