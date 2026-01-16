/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.session.coaching_package.dto;

import com.topleader.topleader.session.coaching_package.CoachingPackage;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateCoachingPackageRequest(
        @NotNull
        CoachingPackage.PackageStatus status,
        @Min(value = 0, message = "Total units must be non-negative")
        int totalUnits
) {
}
