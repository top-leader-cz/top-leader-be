/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.session.coaching_package.dto;

import com.topleader.topleader.session.coaching_package.CoachingPackage;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;


/**
 * Request DTO for creating a coaching package.
 */
public record CreateCoachingPackageRequest(
        @NotNull(message = "Pool type is required")
        CoachingPackage.PoolType poolType,

        @NotNull(message = "Total units is required")
        @Min(value = 1, message = "Total units must be at least 1")
        Integer totalUnits,

        LocalDateTime validFrom,

        LocalDateTime validTo,

        String contextRef
) {
}
