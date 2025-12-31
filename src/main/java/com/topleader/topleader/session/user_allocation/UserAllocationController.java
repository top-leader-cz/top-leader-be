/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.session.user_allocation;

import com.topleader.topleader.session.coaching_package.CoachingPackageService;
import com.topleader.topleader.session.user_allocation.dto.*;
import com.topleader.topleader.user.User;
import com.topleader.topleader.user.UserDetailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

import static com.topleader.topleader.util.user.UserDetailUtils.isAdmin;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserAllocationController {

    private final UserAllocationService userAllocationService;
    private final CoachingPackageService coachingPackageService;
    private final UserDetailService userDetailService;

    @PostMapping("/api/latest/coaching-packages/{packageId}/allocations/{userId}")
    @Secured({"HR", "ADMIN"})
    public UserAllocationDto createAllocation(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long packageId,
            @PathVariable String userId,
            @RequestBody @Valid AllocationRequest request
    ) {
        validatePackageAccess(user, packageId);
        return userAllocationService.createAllocation(packageId, userId, request, user.getUsername());
    }

    @PutMapping("/api/latest/coaching-packages/{packageId}/allocations/{userId}")
    @Secured({"HR", "ADMIN"})
    public UserAllocationDto updateAllocation(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long packageId,
            @PathVariable String userId,
            @RequestBody @Valid AllocationRequest request
    ) {
        validatePackageAccess(user, packageId);
        return userAllocationService.updateAllocation(packageId, userId, request, user.getUsername());
    }

    @PostMapping("/api/latest/coaching-packages/{packageId}/allocations:bulk")
    @Secured({"HR", "ADMIN"})
    public BulkAllocationResponse bulkAllocate(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long packageId,
            @RequestBody @Valid BulkAllocationRequest request
    ) {
        validatePackageAccess(user, packageId);
        return userAllocationService.bulkAllocate(packageId, request, user.getUsername());
    }

    @GetMapping("/api/latest/coaching-packages/{packageId}/allocations")
    @Secured({"HR", "ADMIN"})
    public List<UserAllocationDto> listAllocations(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long packageId
    ) {
        validatePackageAccess(user, packageId);
        return userAllocationService.listAllocations(packageId);
    }

    @PatchMapping("/api/latest/allocations/{allocationId}")
    @Secured({"HR", "ADMIN"})
    public UserAllocationDto updateAllocationStatus(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long allocationId,
            @RequestBody @Valid UpdateAllocationStatusRequest request
    ) {
        var allocation = userAllocationService.getAllocationEntity(allocationId);
        validateCompanyAccess(user, allocation.getCompanyId());
        return userAllocationService.updateAllocationStatus(allocationId, request, user.getUsername());
    }

    @PostMapping("/api/latest/coaching-packages/{packageId}/allocations/{userId}:consume")
    @Secured({"HR", "ADMIN"})
    public UserAllocationDto consumeUnit(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long packageId,
            @PathVariable String userId
    ) {
        validatePackageAccess(user, packageId);
        var allocation = userAllocationService.consumeUnit(packageId, userId);
        return UserAllocationDto.from(allocation);
    }

    private void validatePackageAccess(UserDetails user, Long packageId) {
        var pkg = coachingPackageService.getPackageEntity(packageId);
        validateCompanyAccess(user, pkg.getCompanyId());
    }

    private void validateCompanyAccess(UserDetails user, Long companyId) {
        if (isAdmin(user)) {
            return;
        }

        var userCompanyId = userDetailService.getUser(user.getUsername())
                .map(User::getCompanyId)
                .orElse(null);

        if (!Objects.equals(userCompanyId, companyId)) {
            log.error("User {} attempted to access company {} but belongs to company {}",
                    user.getUsername(), companyId, userCompanyId);
            throw new AccessDeniedException("User does not have access to this company");
        }
    }
}
