/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.admin;

import com.topleader.topleader.user.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
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

    private final AdminViewRepository repository;

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
            AdminViewSpecifications.isTrialEquals(isTrial).ifPresent(specs::add);

            return specs;
        }
    }

}
