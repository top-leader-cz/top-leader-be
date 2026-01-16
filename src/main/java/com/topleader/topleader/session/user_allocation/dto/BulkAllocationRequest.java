/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.session.user_allocation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record BulkAllocationRequest(
        @NotEmpty(message = "Items list cannot be empty")
        @Valid
        List<BulkAllocationItem> items
) {

    public record BulkAllocationItem(
            @NotNull(message = "User ID is required")
            String userId,

            @NotNull(message = "Allocated units is required")
            @Min(value = 0, message = "Allocated units must be >= 0")
            Integer allocatedUnits
    ) {
    }
}
