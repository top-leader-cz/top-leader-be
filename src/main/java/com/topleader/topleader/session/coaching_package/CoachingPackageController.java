/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.session.coaching_package;

import com.topleader.topleader.session.coaching_package.dto.CoachingPackageDto;
import com.topleader.topleader.session.coaching_package.dto.CreateCoachingPackageRequest;
import com.topleader.topleader.session.coaching_package.dto.UpdateCoachingPackageRequest;
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

import static com.topleader.topleader.common.util.user.UserDetailUtils.isAdmin;


/**
 * Controller for managing coaching packages.
 *
 * RBAC:
 * - HR: can create/read/update packages for their company
 * - ADMIN (PlatformAdmin): can create/read/update packages for any company
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class CoachingPackageController {

    private final CoachingPackageService coachingPackageService;
    private final UserDetailService userDetailService;


    @PostMapping("/api/latest/companies/{companyId}/coaching-packages")
    @Secured({"ADMIN"})
    public CoachingPackageDto createPackage(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long companyId,
            @RequestBody @Valid CreateCoachingPackageRequest request
    ) {
        validateCompanyAccess(user, companyId);
        return coachingPackageService.createPackage(companyId, request, user.getUsername());
    }


    @GetMapping("/api/latest/companies/{companyId}/coaching-packages")
    @Secured({"HR", "ADMIN"})
    public List<CoachingPackageDto> listPackages(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long companyId
    ) {
        validateCompanyAccess(user, companyId);
        return coachingPackageService.listPackagesByCompany(companyId);
    }


    @GetMapping("/api/latest/coaching-packages/{packageId}")
    @Secured({"HR", "ADMIN"})
    public CoachingPackageDto getPackage(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long packageId
    ) {
        var entity = coachingPackageService.getPackageEntity(packageId);
        validateCompanyAccess(user, entity.getCompanyId());
        return coachingPackageService.getPackage(packageId);
    }


    @PatchMapping("/api/latest/coaching-packages/{packageId}")
    @Secured({"ADMIN"})
    public CoachingPackageDto updatePackage(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long packageId,
            @RequestBody @Valid UpdateCoachingPackageRequest request
    ) {
        var entity = coachingPackageService.getPackageEntity(packageId);
        validateCompanyAccess(user, entity.getCompanyId());
        return coachingPackageService.updatePackage(packageId, request, user.getUsername());
    }

    private void validateCompanyAccess(UserDetails user, Long companyId) {
        if (isAdmin(user)) {
            // Admin has cross-company access
            return;
        }

        // For HR, check if they belong to the company
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
