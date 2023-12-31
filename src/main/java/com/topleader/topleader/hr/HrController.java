/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.hr;

import com.topleader.topleader.exception.ApiValidationException;
import com.topleader.topleader.exception.NotFoundException;
import com.topleader.topleader.user.InvitationService;
import com.topleader.topleader.user.User;
import com.topleader.topleader.user.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.topleader.topleader.exception.ErrorCodeConstants.EMAIL_USED;
import static com.topleader.topleader.exception.ErrorCodeConstants.NOT_PART_OF_COMPANY;
import static com.topleader.topleader.util.user.UserDetailUtils.isHr;


/**
 * @author Daniel Slavik
 */
@Slf4j
@RestController
@RequestMapping("/api/latest/hr-users")
@AllArgsConstructor
public class HrController {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final InvitationService invitationService;


    @Secured({"HR", "USER"})
    @GetMapping
    public List<HrUserDto> listUsers(@AuthenticationPrincipal UserDetails user) {

        final var dbUser = userRepository.findById(user.getUsername()).orElseThrow();

        if (isHr(user)) {
            return Optional.ofNullable(dbUser.getCompanyId()).map(userRepository::findAllByCompanyId).map(HrUserDto::from).orElseGet(() -> {
                log.info("User {} is not part of any company. Returning an empty list.", user.getUsername()); return List.of();
            });
        }

        return List.of(HrUserDto.from(dbUser));
    }

    @Secured("HR")
    @Transactional
    @PostMapping
    public HrUserDto inviteUser(
        @AuthenticationPrincipal UserDetails user, @Valid @RequestBody UserInvitationRequestDto request
    ) {

        final var hrUser = userRepository.findById(user.getUsername()).orElseThrow();

        final var companyId = Optional.of(hrUser).map(User::getCompanyId).orElseThrow(() ->
            new ApiValidationException(NOT_PART_OF_COMPANY, "user", hrUser.getUsername(), "User is not part of any company")
        );


        if (userRepository.findById(request.email()).isPresent()) {
            throw new ApiValidationException(EMAIL_USED, "email", request.email(), "Already used");
        }


        final var createdUser = userRepository.save(
            new User()
                .setUsername(request.email())
                .setPassword(passwordEncoder.encode(UUID.randomUUID().toString()))
                .setFirstName(request.firstName())
                .setLastName(request.lastName())
                .setCompanyId(companyId)
                .setAuthorities(Set.of(User.Authority.USER))
                .setCredit(0)
                .setTimeZone(hrUser.getTimeZone())
                .setRequestedBy(user.getUsername())
                .setStatus(User.Status.PENDING)
                .setLocale(request.locale())
        );

        invitationService.sendInvite(InvitationService.UserInvitationRequestDto.from(createdUser, request.locale()));

        return HrUserDto.from(createdUser);
    }

    @Secured({"HR", "USER"})
    @Transactional
    @PostMapping("/{username}/credit-request")
    public HrUserDto requestCredits(
        @AuthenticationPrincipal UserDetails user, @PathVariable String username, @RequestBody CreditRequestDto request
    ) {

        if (user.getUsername().equalsIgnoreCase(username)) {
            return HrUserDto.from(
                userRepository.findById(user.getUsername())
                    .map(u -> u.setRequestedCredit(request.credit()))
                    .map(userRepository::save)
                    .orElseThrow(NotFoundException::new)
            );
        }

        if (!isHr(user)) {
            throw new AccessDeniedException("User is not allowed to request credits for another user");
        }

        final var companyId = userRepository.findById(user.getUsername()).map(User::getCompanyId).orElseThrow(NotFoundException::new);

        return HrUserDto.from(
            userRepository.findById(username)
                .filter(u -> companyId.equals(u.getCompanyId()))
                .map(u -> u.setRequestedCredit(request.credit()))
                .map(userRepository::save)
                .orElseThrow(NotFoundException::new)
        );
    }

    public record UserInvitationRequestDto(@NotEmpty String email, @NotEmpty String firstName, @NotEmpty String lastName, Boolean isTrial,
                                           @Pattern(regexp = "[a-z]{2}") String locale) {
    }

    public record CreditRequestDto(@NotNull Integer credit) {
    }

    public record HrUserDto(String firstName, String lastName, String username, String coach, Integer credit, Integer requestedCredit, Integer scheduledCredit, Integer paidCredit,
                            User.Status state) {

        public static List<HrUserDto> from(List<User> users) {
            return users.stream().map(HrUserDto::from).toList();
        }

        public static HrUserDto from(User user) {
            return new HrUserDto(
                user.getFirstName(),
                user.getLastName(),
                user.getUsername(),
                user.getCoach(),
                user.getCredit(),
                user.getRequestedCredit(),
                user.getScheduledCredit(),
                user.getPaidCredit(),
                user.getStatus());
        }

    }
}
