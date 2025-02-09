/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.admin;

import com.topleader.topleader.coach.Coach;
import com.topleader.topleader.coach.CoachRepository;
import com.topleader.topleader.credit.CreditService;
import com.topleader.topleader.exception.NotFoundException;
import com.topleader.topleader.user.InvitationService;
import com.topleader.topleader.user.User;
import com.topleader.topleader.user.UserDetailService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import static com.topleader.topleader.util.user.UserDetailUtils.sendInvite;
import static java.util.Objects.isNull;
import static java.util.function.Predicate.not;


/**
 * @author Daniel Slavik
 */
@Slf4j
@RestController
@RequestMapping("/api/latest/admin")
@AllArgsConstructor
public class AdminViewController {

    private final AdminViewRepository repository;

    private final InvitationService invitationService;

    private final CreditService creditService;

    private final UserDetailService userDetailService;

    private final CoachRepository coachRepository;

    @Secured("ADMIN")
    @GetMapping("/users")
    public Page<AdminView> getUsers(
        FilterDto filterDto,
        Pageable pageable
    ) {

        return Optional.ofNullable(filterDto)
            .map(FilterDto::toSpecifications)
            .filter(not(List::isEmpty))
            .map(filter -> repository.findAll(Specification.allOf(filter), pageable))
            .orElseGet(() -> repository.findAll(pageable));
    }

    @Transactional
    @Secured("ADMIN")
    @PostMapping("/users")
    public void createUser(@AuthenticationPrincipal UserDetails u, @RequestBody @Valid CreateUserRequestDto userRequest) {
        final var user = userDetailService.save(userRequest.toUser(u.getUsername()));
        userRequest.toCoach().ifPresent(coachRepository::save);
        if (sendInvite(User.Status.PENDING, userRequest.status)) {
            invitationService.sendInvite(InvitationService.UserInvitationRequestDto.from(user, userRequest.locale()));
        }
    }

    @Transactional
    @Secured("ADMIN")
    @PostMapping("/users/{username}")
    public void updateUser(@PathVariable String username, @RequestBody @Valid UpdateUserRequestDto userRequest) {

        userDetailService.getUser(username)
            .map(user -> {
                var oldStatus = user.getStatus();
                var updatedUser = userRequest.updateUser(user);
                if (sendInvite(oldStatus, userRequest.status())) {
                    invitationService.sendInvite(InvitationService.UserInvitationRequestDto.from(updatedUser, userRequest.locale()));
                }
                return user;
            })
            .ifPresentOrElse(
                userDetailService::save,
                () -> {
                    throw new NotFoundException();
                }
            );

        coachRepository.findById(username)
            .map(userRequest::updateCoach)
            .ifPresentOrElse(
                coachRepository::save,
                () -> userRequest.toCoach(username).ifPresent(coachRepository::save));

    }

    @Transactional
    @Secured("ADMIN")
    @PostMapping("/users/{username}/resent-invitation")
    public void resentInvitation(@PathVariable String username, @RequestBody @Valid ResentInvitationRequestDto userRequest) {

        userDetailService.getUser(username)
            .ifPresent(user -> invitationService.sendInvite(InvitationService.UserInvitationRequestDto.from(user, userRequest.locale())));

    }

    @Transactional
    @Secured("ADMIN")
    @PostMapping("/users/{username}/confirm-requested-credits")
    public void topUpCredits(@PathVariable String username) {
        creditService.topUpCredit(username);
    }


    @Secured("ADMIN")
    @DeleteMapping("/users/{username}")
    public void deleteUser(@PathVariable String username) {
        userDetailService.delete(username);
    }

    public record ResentInvitationRequestDto(
        @Pattern(regexp = "[a-z]{2}")
        String locale
    ) {
    }

    public record UpdateUserRequestDto(
        String firstName,
        String lastName,
        String timeZone,
        Long companyId,
        Boolean isTrial,
        Set<User.Authority> authorities,
        User.Status status,
        String coach,
        Integer credit,
        String freeCoach,
        @Pattern(regexp = "[a-z]{2}")
        String locale,
        Set<String> allowedCoachRates,
        String rate,
        Integer internalRate
    ) {
        public User updateUser(User user) {
            Optional.ofNullable(firstName).ifPresent(user::setFirstName);
            Optional.ofNullable(lastName).ifPresent(user::setLastName);
            Optional.ofNullable(timeZone).ifPresent(user::setTimeZone);
            user.setCompanyId(companyId);
            Optional.ofNullable(authorities).ifPresent(user::setAuthorities);
            Optional.ofNullable(status).ifPresent(user::setStatus);
            user.setCoach(coach);
            Optional.ofNullable(credit).ifPresent(user::setCredit);
            user.setFreeCoach(freeCoach);
            Optional.ofNullable(locale).ifPresent(user::setLocale);
            user.setAllowedCoachRates(allowedCoachRates);
            return user;
        }

        public Coach updateCoach(Coach coach) {
            Optional.ofNullable(rate).ifPresent(coach::setRate);
            Optional.ofNullable(internalRate).ifPresent(coach::setInternalRate);
            return coach;
        }

        public Optional<Coach> toCoach(String username) {
            if (isNull(rate) && isNull(internalRate)) {
                return Optional.empty();
            }

            return Optional.of(
                new Coach()
                    .setUsername(username)
                    .setRate(rate)
                    .setInternalRate(internalRate)
            );
        }
    }

    public record CreateUserRequestDto(
            @NotBlank @Email String username,
            @NotBlank String firstName,
            @NotBlank String lastName,
            @NotBlank String timeZone,
            Long companyId,
            @NotNull Boolean isTrial,

            Set<User.Authority> authorities,
            @NotNull User.Status status,
            @Pattern(regexp = "[a-z]{2}")
            String locale,
            @NotNull
            String rate,
            Integer internalRate,
            @NotNull
            Coach.CertificateType certificate
            ) {
        public User toUser(String requestedBy) {
            return new User()
                .setUsername(username.toLowerCase(Locale.ROOT))
                .setFirstName(firstName)
                .setLastName(lastName)
                .setTimeZone(timeZone)
                .setCompanyId(companyId)
                .setAuthorities(authorities)
                .setRequestedBy(requestedBy)
                .setLocale(locale)
                .setStatus(status);
        }

        public Optional<Coach> toCoach() {
            return Optional.of(
                new Coach()
                    .setUsername(username.toLowerCase(Locale.ROOT))
                    .setRate(rate)
                    .setCertificate(certificate)
                    .setInternalRate(internalRate)
            );
        }
    }

    public record FilterDto(
        String username,
        String firstName,
        String lastName,
        String timeZone,
        User.Status status,
        Long companyId,
        String companyName,
        String coach,
        String coachFirstName,
        String coachLastName,
        Integer credit,
        Integer requestedCredit,
        Integer sumRequestedCredit,
        Integer paidCredit,
        String hrs,
        String requestedBy,
        Boolean isTrial,

        String freeCoach,
        String maxCoachRate,
        Boolean showCanceled
    ) {

        public List<Specification<AdminView>> toSpecifications() {
            List<Specification<AdminView>> specs = new ArrayList<>();

            AdminViewSpecifications.usernameEquals(username).ifPresent(specs::add);
            AdminViewSpecifications.firstNameContains(firstName).ifPresent(specs::add);
            AdminViewSpecifications.lastNameEquals(lastName).ifPresent(specs::add);
            AdminViewSpecifications.timeZoneEquals(timeZone).ifPresent(specs::add);
            AdminViewSpecifications.statusEquals(status).ifPresent(specs::add);
            AdminViewSpecifications.companyIdEquals(companyId).ifPresent(specs::add);
            AdminViewSpecifications.companyNameContains(companyName).ifPresent(specs::add);
            AdminViewSpecifications.coachContains(coach).ifPresent(specs::add);
            AdminViewSpecifications.coachFirstNameContains(coachFirstName).ifPresent(specs::add);
            AdminViewSpecifications.coachLastNameContains(coachLastName).ifPresent(specs::add);
            AdminViewSpecifications.creditEquals(credit).ifPresent(specs::add);
            AdminViewSpecifications.requestedCreditEquals(requestedCredit).ifPresent(specs::add);
            AdminViewSpecifications.sumRequestedCreditEquals(sumRequestedCredit).ifPresent(specs::add);
            AdminViewSpecifications.paidCreditEquals(paidCredit).ifPresent(specs::add);
            AdminViewSpecifications.hrsContains(hrs).ifPresent(specs::add);
            AdminViewSpecifications.requestedByContains(requestedBy).ifPresent(specs::add);
            AdminViewSpecifications.isTrialEquals(isTrial).ifPresent(specs::add);
            AdminViewSpecifications.freeCoach(freeCoach).ifPresent(specs::add);
            AdminViewSpecifications.maxCoachRateContains(maxCoachRate).ifPresent(specs::add);
            AdminViewSpecifications.showCanceled(showCanceled).ifPresent(specs::add);

            return specs;
        }
    }

}
