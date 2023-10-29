/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.admin;

import com.topleader.topleader.credit.CreditService;
import com.topleader.topleader.exception.NotFoundException;
import com.topleader.topleader.user.InvitationService;
import com.topleader.topleader.user.User;
import com.topleader.topleader.user.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static java.util.function.Predicate.not;


/**
 * @author Daniel Slavik
 */
@Slf4j
@RestController
@RequestMapping("/api/latest/admin")
@AllArgsConstructor
public class AdminViewController {

    private final UserRepository userRepository;

    private final AdminViewRepository repository;

    private final InvitationService invitationService;

    private final CreditService creditService;

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
        final var user = userRepository.save(userRequest.toUser(u.getUsername()));
        invitationService.sendInvite(InvitationService.UserInvitationRequestDto.from(user, null));
    }

    @Transactional
    @Secured("ADMIN")
    @PostMapping("/users/{username}")
    public void createUser(@PathVariable String username, @RequestBody UpdateUserRequestDto userRequest) {

        userRepository.findById(username)
            .map(userRequest::updateUser)
            .ifPresentOrElse(
                userRepository::save,
                () -> {
                    throw new NotFoundException();
                }
            );

    }

    @Transactional
    @Secured("ADMIN")
    @PostMapping("/users/{username}/confirm-requested-credits")
    public void topUpCredits(@PathVariable String username) {
        creditService.topUpCredit(username);
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
        Integer credit
    ) {
        public User updateUser(User user) {
            Optional.ofNullable(firstName).ifPresent(user::setFirstName);
            Optional.ofNullable(lastName).ifPresent(user::setLastName);
            Optional.ofNullable(timeZone).ifPresent(user::setTimeZone);
            Optional.ofNullable(companyId).ifPresent(user::setCompanyId);
            Optional.ofNullable(isTrial).ifPresent(user::setIsTrial);
            Optional.ofNullable(authorities).ifPresent(user::setAuthorities);
            Optional.ofNullable(status).ifPresent(user::setStatus);
            Optional.ofNullable(coach).ifPresent(user::setCoach);
            Optional.ofNullable(credit).ifPresent(user::setCredit);
            return user;
        }
    }

    public record CreateUserRequestDto(
        @NotBlank String username,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank String timeZone,
        Long companyId,
        @NotNull Boolean isTrial,

        Set<User.Authority> authorities
    ) {
        public User toUser(String requestedBy) {
            return new User()
                .setUsername(username)
                .setFirstName(firstName)
                .setLastName(lastName)
                .setTimeZone(timeZone)
                .setCompanyId(companyId)
                .setIsTrial(isTrial)
                .setAuthorities(authorities)
                .setRequestedBy(requestedBy)
                .setStatus(User.Status.PENDING);
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
        Integer paidCredit,
        String hrs,
        String requestedBy,
        Boolean isTrial) {

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
            AdminViewSpecifications.paidCreditEquals(paidCredit).ifPresent(specs::add);
            AdminViewSpecifications.hrsContains(hrs).ifPresent(specs::add);
            AdminViewSpecifications.requestedByContains(requestedBy).ifPresent(specs::add);
            AdminViewSpecifications.isTrialEquals(isTrial).ifPresent(specs::add);

            return specs;
        }
    }

}
