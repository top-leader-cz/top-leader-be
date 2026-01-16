/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.session.user_allocation.dto;

import com.topleader.topleader.session.user_allocation.UserAllocation;

import java.util.List;

public record BulkAllocationResponse(
        int updated,
        List<BulkAllocationItemResponse> items
) {

    public record BulkAllocationItemResponse(
            String userId,
            int allocatedUnits,
            UserAllocation.AllocationStatus status
    ) {
    }
}
