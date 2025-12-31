/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.session.coaching_package.dto;


/**
 * DTO representing metrics for a coaching package.
 *
 * Note: In V1 (Epic 1), reserved_units and consumed_units return 0.
 * These will be computed from bookings table in Epic 2.
 */
public record CoachingPackageMetricsDto(
        int totalUnits,
        int allocatedUnits,
        int reservedUnits,
        int consumedUnits,
        int remainingUnits,
        int unallocatedUnits
) {

    /**
     * Creates metrics with all computed fields.
     *
     * @param totalUnits total units in the package
     * @param allocatedUnits sum of active user allocations
     * @param reservedUnits units reserved by scheduled bookings (0 until Epic 2)
     * @param consumedUnits units consumed by completed/no-show bookings (0 until Epic 2)
     * @return computed metrics
     */
    public static CoachingPackageMetricsDto of(int totalUnits, int allocatedUnits, int reservedUnits, int consumedUnits) {
        int remainingUnits = totalUnits - reservedUnits - consumedUnits;
        int unallocatedUnits = totalUnits - allocatedUnits - reservedUnits - consumedUnits;
        return new CoachingPackageMetricsDto(
                totalUnits,
                allocatedUnits,
                reservedUnits,
                consumedUnits,
                remainingUnits,
                unallocatedUnits
        );
    }

    /**
     * Creates metrics for V1 where reserved and consumed are always 0.
     *
     * @param totalUnits total units in the package
     * @param allocatedUnits sum of active user allocations
     * @return computed metrics with reserved/consumed = 0
     */
    public static CoachingPackageMetricsDto forV1(int totalUnits, int allocatedUnits) {
        return of(totalUnits, allocatedUnits, 0, 0);
    }
}
