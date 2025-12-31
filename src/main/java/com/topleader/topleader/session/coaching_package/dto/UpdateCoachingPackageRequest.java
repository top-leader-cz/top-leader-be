/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.session.coaching_package.dto;

import com.topleader.topleader.session.coaching_package.CoachingPackage;
import jakarta.validation.constraints.NotNull;


/**
 * Request DTO for updating a coaching package (e.g., deactivating).
 */
public record UpdateCoachingPackageRequest(
        @NotNull(message = "Status is required")
        CoachingPackage.PackageStatus status
) {
}
