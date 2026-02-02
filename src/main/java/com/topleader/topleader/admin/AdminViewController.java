/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.admin;

import com.topleader.topleader.coach.Coach;
import com.topleader.topleader.coach.CoachRepository;
import com.topleader.topleader.common.exception.NotFoundException;
import com.topleader.topleader.user.InvitationService;
import com.topleader.topleader.user.User;
import com.topleader.topleader.user.UserDetailService;
import com.topleader.topleader.user.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import static com.topleader.topleader.common.util.user.UserDetailUtils.sendInvite;


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

    private final UserDetailService userDetailService;

    private final CoachRepository coachRepository;

    private final UserRepository userRepository;

    @Secured("ADMIN")
    @GetMapping("/users")
    public Page<AdminView> getUsers(
        FilterDto filterDto,
        Pageable pageable
    ) {
        var filter = Optional.ofNullable(filterDto).orElse(new FilterDto(
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null
        ));
        var allFiltered = repository.findFiltered(
                filter.username(),
                filter.firstName(),
                filter.lastName(),
                filter.timeZone(),
                Optional.ofNullable(filter.status()).map(Enum::name).orElse(null),
                filter.companyId(),
                filter.companyName(),
                filter.coach(),
                filter.coachFirstName(),
                filter.coachLastName(),
                filter.credit(),
                filter.requestedCredit(),
                filter.sumRequestedCredit(),
                filter.paidCredit(),
                filter.hrs(),
                filter.requestedBy(),
                filter.freeCoach(),
                filter.maxCoachRate(),
                filter.showCanceled(),
                pageable
        );

        var sorted = pageable.getSort().isSorted()
                ? sortResults(allFiltered, pageable)
                : allFiltered;

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), sorted.size());
        var pageContent = start < sorted.size() ? sorted.subList(start, end) : List.<AdminView>of();

        return new PageImpl<>(pageContent, pageable, sorted.size());
    }

    private List<AdminView> sortResults(List<AdminView> results, Pageable pageable) {
        return results.stream()
                .sorted((a, b) -> {
                    for (var order : pageable.getSort()) {
                        var comparison = compareByField(a, b, order.getProperty());
                        if (comparison != 0) {
                            return order.isAscending() ? comparison : -comparison;
                        }
                    }
                    return 0;
                })
                .toList();
    }

    @SuppressWarnings("unchecked")
    private int compareByField(AdminView a, AdminView b, String field) {
        return switch (field) {
            case "username" -> compareNullSafe(a.getUsername(), b.getUsername());
            case "firstName" -> compareNullSafe(a.getFirstName(), b.getFirstName());
            case "lastName" -> compareNullSafe(a.getLastName(), b.getLastName());
            case "status" -> compareNullSafe(
                    Optional.ofNullable(a.getStatus()).map(Enum::name).orElse(null),
                    Optional.ofNullable(b.getStatus()).map(Enum::name).orElse(null));
            case "companyName" -> compareNullSafe(a.getCompanyName(), b.getCompanyName());
            case "credit" -> compareNullSafe(a.getCredit(), b.getCredit());
            default -> 0;
        };
    }

    private <T extends Comparable<T>> int compareNullSafe(T a, T b) {
        if (a == null && b == null) return 0;
        if (a == null) return 1;
        if (b == null) return -1;
        return a.compareTo(b);
    }

    @Secured("ADMIN")
    @PostMapping("/users")
    public void createUser(@AuthenticationPrincipal UserDetails u, @RequestBody @Valid CreateUserRequestDto userRequest) {
        final var user = userDetailService.save(userRequest.toUser(u.getUsername()));
        userRequest.toCoach().ifPresent(coachRepository::save);
        if (sendInvite(User.Status.PENDING, userRequest.status)) {
            invitationService.sendInvite(InvitationService.UserInvitationRequestDto.from(user, userRequest.locale()));
        }
    }

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
                user -> {
                    userDetailService.save(user);
                    updateAllowedCoachRates(username, userRequest.allowedCoachRates());
                },
                () -> {
                    throw new NotFoundException();
                }
            );

        coachRepository.findByUsername(username)
            .map(userRequest::updateCoach)
            .ifPresentOrElse(
                coachRepository::save,
                () -> userRequest.toCoach(username).ifPresent(coachRepository::save));

    }

    @Secured("ADMIN")
    @PostMapping("/users/{username}/resent-invitation")
    public void resentInvitation(@PathVariable String username, @RequestBody @Valid ResentInvitationRequestDto userRequest) {

        userDetailService.getUser(username)
            .ifPresent(user -> invitationService.sendInvite(InvitationService.UserInvitationRequestDto.from(user, userRequest.locale())));

    }

    @Secured("ADMIN")
    @DeleteMapping("/users/{username}")
    public void deleteUser(@PathVariable String username) {
        userDetailService.delete(username);
    }

    private void updateAllowedCoachRates(String username, Set<String> rates) {
        userRepository.deleteAllowedCoachRates(username);
        if (rates != null) {
            rates.forEach(rate -> userRepository.insertAllowedCoachRate(username, rate));
        }
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
        Integer internalRate,
        Set<String> certificate
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
            Optional.ofNullable(certificate).ifPresent(coach::setCertificateSet);
            return coach;
        }

        public Optional<Coach> toCoach(String username) {
            return Optional.of(
                new Coach()
                    .setUsername(username)
                    .setRate(rate)
                    .setInternalRate(internalRate)
                    .setCertificateSet(certificate)
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
            String rate,
            Integer internalRate,
            Set<String> certificate
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
                    .setCertificateSet(certificate)
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
    }

}
