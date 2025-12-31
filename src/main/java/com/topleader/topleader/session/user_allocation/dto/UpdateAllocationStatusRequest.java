/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.session.user_allocation.dto;

import com.topleader.topleader.session.user_allocation.UserAllocation;
import jakarta.validation.constraints.NotNull;

public record UpdateAllocationStatusRequest(
        @NotNull(message = "Status is required")
        UserAllocation.AllocationStatus status
) {
}
