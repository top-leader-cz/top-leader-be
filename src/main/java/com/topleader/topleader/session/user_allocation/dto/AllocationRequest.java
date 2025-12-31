/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.session.user_allocation.dto;

import com.topleader.topleader.session.user_allocation.UserAllocation;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AllocationRequest(
        @NotNull(message = "Allocated units is required")
        @Min(value = 0, message = "Allocated units must be >= 0")
        Integer allocatedUnits,

        UserAllocation.AllocationStatus status,

        String contextRef
) {
}
