/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.admin;


import com.topleader.topleader.user.User;
import java.util.Optional;

import io.vavr.collection.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import static com.topleader.topleader.user.User.Status.*;


/**
 * @author Daniel Slavik
 */
public final class AdminViewSpecifications {

    private AdminViewSpecifications() {
    }

    public static Optional<Specification<AdminView>> freeCoach(String coach) {
        if (!StringUtils.hasText(coach)) {
            return Optional.empty(); // Empty filter, return empty Optional
        }
        return Optional.of((root, query, criteriaBuilder) ->
            criteriaBuilder.equal(root.get("freeCoach"), coach));
    }

    public static Optional<Specification<AdminView>> usernameEquals(String username) {
        if (!StringUtils.hasText(username)) {
            return Optional.empty(); // Empty filter, return empty Optional
        }
        return Optional.of((root, query, criteriaBuilder) ->
            criteriaBuilder.equal(root.get("username"), username));
    }

    public static Optional<Specification<AdminView>> firstNameContains(String firstName) {
        if (!StringUtils.hasText(firstName)) {
            return Optional.empty(); // Empty filter, return empty Optional
        }
        return Optional.of((root, query, criteriaBuilder) ->
            criteriaBuilder.like(root.get("firstName"), "%" + firstName + "%"));
    }

    public static Optional<Specification<AdminView>> lastNameEquals(String lastName) {
        if (!StringUtils.hasText(lastName)) {
            return Optional.empty(); // Empty filter, return empty Optional
        }
        return Optional.of((root, query, criteriaBuilder) ->
            criteriaBuilder.equal(root.get("lastName"), lastName));
    }

    public static Optional<Specification<AdminView>> timeZoneEquals(String timeZone) {
        if (!StringUtils.hasText(timeZone)) {
            return Optional.empty(); // Empty filter, return empty Optional
        }
        return Optional.of((root, query, criteriaBuilder) ->
            criteriaBuilder.equal(root.get("timeZone"), timeZone));
    }

    public static Optional<Specification<AdminView>> statusEquals(User.Status status) {
        return Optional.ofNullable(status)
            .map(c -> (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), c));
    }

    public static Optional<Specification<AdminView>> companyIdEquals(Long companyId) {
        return Optional.ofNullable(companyId)
            .map(c -> (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("companyId"), c));
    }

    public static Optional<Specification<AdminView>> companyNameContains(String companyName) {
        if (!StringUtils.hasText(companyName)) {
            return Optional.empty(); // Empty filter, return empty Optional
        }
        return Optional.of((root, query, criteriaBuilder) ->
            criteriaBuilder.like(root.get("companyName"), "%" + companyName + "%"));
    }

    public static Optional<Specification<AdminView>> coachContains(String coach) {
        if (!StringUtils.hasText(coach)) {
            return Optional.empty(); // Empty filter, return empty Optional
        }
        return Optional.of((root, query, criteriaBuilder) ->
            criteriaBuilder.like(root.get("coach"), "%" + coach + "%"));
    }

    public static Optional<Specification<AdminView>> coachFirstNameContains(String coachFirstName) {
        if (!StringUtils.hasText(coachFirstName)) {
            return Optional.empty(); // Empty filter, return empty Optional
        }
        return Optional.of((root, query, criteriaBuilder) ->
            criteriaBuilder.like(root.get("coachFirstName"), "%" + coachFirstName + "%"));
    }

    public static Optional<Specification<AdminView>> coachLastNameContains(String coachLastName) {
        if (!StringUtils.hasText(coachLastName)) {
            return Optional.empty(); // Empty filter, return empty Optional
        }
        return Optional.of((root, query, criteriaBuilder) ->
            criteriaBuilder.like(root.get("coachLastName"), "%" + coachLastName + "%"));
    }

    public static Optional<Specification<AdminView>> creditEquals(Integer credit) {
        return Optional.ofNullable(credit)
            .map(c -> (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("credit"), c));
    }

    public static Optional<Specification<AdminView>> requestedCreditEquals(Integer requestedCredit) {
        return Optional.ofNullable(requestedCredit)
            .map(r -> (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("requestedCredit"), r));
    }

    public static Optional<Specification<AdminView>> sumRequestedCreditEquals(Integer requestedCredit) {
        return Optional.ofNullable(requestedCredit)
            .map(r -> (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("sumRequestedCredit"), r));
    }

    public static Optional<Specification<AdminView>> paidCreditEquals(Integer paidCredit) {
        return Optional.ofNullable(paidCredit)
            .map(r -> (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("paidCredit"), r));
    }

    public static Optional<Specification<AdminView>> hrsContains(String hrs) {
        if (!StringUtils.hasText(hrs)) {
            return Optional.empty(); // Empty filter, return empty Optional
        }
        return Optional.of((root, query, criteriaBuilder) ->
            criteriaBuilder.like(root.get("hrs"), "%" + hrs + "%"));
    }

    public static Optional<Specification<AdminView>> requestedByContains(String requestedBy) {
        if (!StringUtils.hasText(requestedBy)) {
            return Optional.empty(); // Empty filter, return empty Optional
        }
        return Optional.of((root, query, criteriaBuilder) ->
            criteriaBuilder.like(root.get("requestedBy"), "%" + requestedBy + "%"));
    }

    public static Optional<Specification<AdminView>> maxCoachRateContains(String maxCoachRate) {
        if (!StringUtils.hasText(maxCoachRate)) {
            return Optional.empty(); // Empty filter, return empty Optional
        }
        return Optional.of((root, query, criteriaBuilder) ->
            criteriaBuilder.like(root.get("maxCoachRate"), "%" + maxCoachRate + "%"));
    }

    public static Optional<Specification<AdminView>> isTrialEquals(Boolean isTrial) {
        return Optional.ofNullable(isTrial)
            .map(r -> (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("isTrial"), r));
    }

    public static Optional<Specification<AdminView>> showCanceled(Boolean showCanceled) {
        return Optional.ofNullable(showCanceled)
                .map(r -> (root, query, criteriaBuilder) ->
                        root.get("status").in(showCanceled ? getAllUsers() : getActiveUsers()));
    }

    private static User.Status[] getActiveUsers() {
        return new User.Status[] {AUTHORIZED, PENDING, PAID, REQUESTED, VIEWED, SUBMITTED};
    }

    private static User.Status[] getAllUsers() {
        return new User.Status[] {AUTHORIZED, PENDING, PAID, REQUESTED, VIEWED, SUBMITTED, CANCELED};
    }

}


