/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.hr;

import com.topleader.topleader.email.EmailService;
import com.topleader.topleader.exception.ApiValidationException;
import com.topleader.topleader.exception.NotFoundException;
import com.topleader.topleader.hr.domain.*;
import com.topleader.topleader.user.*;
import com.topleader.topleader.user.manager.ManagerService;
import com.topleader.topleader.util.transaction.TransactionService;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import static com.topleader.topleader.exception.ErrorCodeConstants.EMAIL_USED;
import static com.topleader.topleader.exception.ErrorCodeConstants.NOT_PART_OF_COMPANY;
import static com.topleader.topleader.user.User.Status.PENDING;
import static com.topleader.topleader.util.user.UserDetailUtils.isHr;
import static com.topleader.topleader.util.user.UserDetailUtils.sendInvite;


/**
 * @author Daniel Slavik
 */
@Slf4j
@RestController
@RequestMapping("/api/latest/hr-users")
@AllArgsConstructor
public class HrController {

    private final PasswordEncoder passwordEncoder;

    private final InvitationService invitationService;

    private final TransactionService transactionService;

    private final EmailService emailService;

    private final HrService hrService;

    private final UserDetailService userDetailService;

    private final ManagerService managerService;

    @Secured({"HR"})
    @GetMapping
    public List<CreditsDto> listUsers(@AuthenticationPrincipal UserDetails user) {

        var dbUser = hrService.findByUsername(user.getUsername());

        if (isHr(user)) {
            return Optional.ofNullable(dbUser.getCompanyId()).map(hrService::findByCompany).map(CreditsDto::from).orElseGet(() -> {
                log.info("User {} is not part of any company. Returning an empty list.", user.getUsername());
                return List.of();
            });
        }

        return List.of(CreditsDto.from(dbUser));
    }

    @Secured({"HR"})
    @GetMapping("/{username}")
    @Transactional
    public UserDto getUser(@PathVariable String username) {
        return UserDto.fromUser(userDetailService.find(username));
    }

    @Secured({"HR"})
    @GetMapping("/managers")
    @Transactional
    public List<ManagerDto> listManagers(@AuthenticationPrincipal UserDetails user) {
        var foundUser = userDetailService.find(user.getUsername());
        return managerService.listManagerByCompany(foundUser.getCompanyId()).stream()
                .map(m -> {
                    var found = userDetailService.find(m.getUsername());
                    return new ManagerDto(found.getUsername(), found.getFirstName(), found.getLastName());
                })
                .collect(Collectors.toList());
    }

    @Secured({"HR"})
    @PostMapping
    @Transactional
    public UserDto addUser(@AuthenticationPrincipal UserDetails loggedUser, @RequestBody @Valid AddUserRequest request) {

        var user = new User();
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()))
                .setUsername(request.username().toLowerCase(Locale.ROOT))
                .setFirstName(request.firstName())
                .setLastName(request.lastName())
                .setAuthorities(request.authorities())
                .setTimeZone(request.timeZone())
                .setRequestedBy(loggedUser.getUsername())
                .setLocale(request.locale())
                .setStatus(request.status())
                .setLocale(request.locale())
                .setPosition(request.position())
                .setAspiredCompetency(request.aspiredCompetency());

        final var hrUser = userDetailService.getUser(loggedUser.getUsername()).orElseThrow();

        final var companyId = Optional.of(hrUser).map(User::getCompanyId).orElseThrow(() ->
                new ApiValidationException(NOT_PART_OF_COMPANY, "user", hrUser.getUsername(), "User is not part of any company")
        );

        user.setCompanyId(companyId)
                .setCredit(0);

        if (userDetailService.getUser(request.username()).isPresent()) {
            throw new ApiValidationException(EMAIL_USED, "username", request.username(), "Already used");
        }

        var saved = managerService.processUser(user, request);
        if (sendInvite(PENDING, request.status())) {
            invitationService.sendInvite(InvitationService.UserInvitationRequestDto.from(user, request.locale()));
        }
        if (PENDING == request.status()) {
            var body = String.format("Username: %s Timestamp: %s", request.username(), LocalDateTime.now());
            emailService.sendEmail("info@topleader.io", "New Pending user in the TopLeader platform", body);
        }

        return UserDto.fromUser(saved);
    }

    @Secured({"HR"})
    @PutMapping("/{username}")
    @Transactional
    public UserDto updateUser(
            @AuthenticationPrincipal UserDetails loggedUser,
            @PathVariable String username,
            @RequestBody @Valid UpdateUserRequest request
    ) {


        final var hr = userDetailService.getUser(loggedUser.getUsername()).orElseThrow();
        final var userInTheSameCompany = userDetailService.getUser(username)
                .filter(u -> Objects.equals(hr.getCompanyId(), u.getCompanyId()));

        if (userInTheSameCompany.isEmpty()) {
            throw new ApiValidationException(NOT_PART_OF_COMPANY, "username", username, "User is not part of any company");
        }

        final var user = userDetailService.getUser(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        final var oldStatus = user.getStatus();
        user.setStatus(request.status());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setTimeZone(request.timeZone());
        user.setLocale(request.locale());
        user.setAuthorities(request.authorities());
        user.setPosition(request.position());
        user.setAspiredCompetency(request.aspiredCompetency());

        var updatedUser = managerService.processUser(user, request);

        if (sendInvite(oldStatus, request.status())) {
            invitationService.sendInvite(InvitationService.UserInvitationRequestDto.from(updatedUser, request.locale()));
        }

        return UserDto.fromUser(updatedUser);
    }


    @Secured({"HR"})
    @PostMapping("/{username}/credit-request")
    public CreditsDto requestCredits(
            @AuthenticationPrincipal UserDetails user, @PathVariable String username, @RequestBody CreditRequestDto request
    ) {

        if (user.getUsername().equalsIgnoreCase(username)) {
            transactionService.execute(() -> userDetailService.getUser(user.getUsername())
                    .map(u -> u.setRequestedCredit(request.credit()))
                    .map(userDetailService::save)
                    .orElseThrow(NotFoundException::new)
            );

            var body = String.format("Username: %s Amount: %s Timestamp: %s", username, request.credit(), LocalDateTime.now());
            emailService.sendEmail("info@topleader.io", "Credits requested in the TopLeader platform", body);
            return CreditsDto.from(hrService.findByUsername(username));
        }

        if (!isHr(user)) {
            throw new AccessDeniedException("User is not allowed to request credits for another user");
        }

        final var companyId = userDetailService.getUser(user.getUsername()).map(User::getCompanyId).orElseThrow(NotFoundException::new);

        transactionService.execute(() -> userDetailService.getUser(username)
                .filter(u -> companyId.equals(u.getCompanyId()))
                .map(u -> u.setRequestedCredit(request.credit()))
                .map(userDetailService::save)
                .orElseThrow(NotFoundException::new)
        );

        var body = String.format("Hr: %s for username: %s Amount: %s Timestamp: %s", user.getUsername(), username, request.credit(), LocalDateTime.now());
        emailService.sendEmail("info@topleader.io", "Credits requested in the TopLeader platform", body);

        return CreditsDto.from(hrService.findByUsername(username));
    }


}
