/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.hr.hr_report;

import com.topleader.topleader.common.exception.ApiValidationException;
import com.topleader.topleader.session.coaching_package.CoachingPackageRepository;
import com.topleader.topleader.user.User;
import com.topleader.topleader.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

import static com.topleader.topleader.common.exception.ErrorCodeConstants.USER_NO_AUTHORIZED;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/latest/coaching-packages")
public class HrReportController {

    private final HrReportService hrReportService;
    private final CoachingPackageRepository coachingPackageRepository;
    private final UserRepository userRepository;

    @GetMapping("/{packageId}/hr-report")
    @Secured({"HR", "ADMIN"})
    public HrReportResponse getHrReport(
            @PathVariable Long packageId,
            @RequestParam(required = false) ZonedDateTime from,
            @RequestParam(required = false) ZonedDateTime to,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("User {} requesting HR report for package {} (from={}, to={})",
                userDetails.getUsername(), packageId, from, to);

        validateAccess(userDetails, packageId);

        var fromDate = Optional.ofNullable(from).map(z -> z.withZoneSameInstant(ZoneOffset.UTC).toLocalDate()).orElse(null);
        var toDate = Optional.ofNullable(to).map(z -> z.withZoneSameInstant(ZoneOffset.UTC).toLocalDate()).orElse(null);

        return hrReportService.generateReport(packageId, fromDate, toDate);
    }

    private void validateAccess(UserDetails userDetails, Long packageId) {
        var user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        var pkg = coachingPackageRepository.findById(packageId).orElseThrow();

        var isAdmin = user.getAuthorities().contains(User.Authority.ADMIN);
        var isSameCompany = user.getCompanyId() != null && user.getCompanyId().equals(pkg.getCompanyId());

        if (!isAdmin && !isSameCompany) {
            log.error("User {} attempted to access package {} but belongs to company {}",
                    userDetails.getUsername(), packageId, user.getCompanyId());
            throw new ApiValidationException(USER_NO_AUTHORIZED, "packageId", packageId.toString(),
                    "User not authorized to access this package");
        }
    }
}
