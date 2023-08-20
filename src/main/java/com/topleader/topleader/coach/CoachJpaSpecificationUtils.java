/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;


/**
 * @author Daniel Slavik
 */
public final class CoachJpaSpecificationUtils {

    private static final String EXPERIENCE_FIELD = "experienceSince";

    private CoachJpaSpecificationUtils() {
        //util class
    }

    public static Specification<Coach> hasLanguagesInList(List<String> languages) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isTrue(
            root.join("languages")
                .in(languages)
        );
    }

    public static Specification<Coach> hasFieldsInList(List<String> fields) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isTrue(
            root.join("fields")
                .in(fields)
        );
    }

    public static Specification<Coach> hasExperienceBetween(LocalDate from, LocalDate to) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.between(root.get(EXPERIENCE_FIELD), from, to);
    }

    public static Specification<Coach> hasExperienceFrom(LocalDate from) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get(EXPERIENCE_FIELD), from);
    }

    public static Specification<Coach> hasExperienceTo(LocalDate to) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get(EXPERIENCE_FIELD), to);
    }

    public static Specification<Coach> hasRateInSet(List<String> rates) {
        return (root, query, criteriaBuilder) -> root.get("rate").in(rates);
    }

    public static Specification<Coach> nameStartsWith(String name) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.or(
            criteriaBuilder.like(root.get("firstName"), name + "%"),
            criteriaBuilder.like(root.get("lastName"), name + "%")
        );
    }
}
