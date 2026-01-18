/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach;

import com.topleader.topleader.coach.list.CoachListView;
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

    public static Specification<CoachListView> hasLanguagesInList(List<String> languages) {
        return (root, query, criteriaBuilder) -> {
            query.distinct(true);
            var predicates = languages.stream()
                .map(lang -> criteriaBuilder.isTrue(
                    criteriaBuilder.function(
                        "jsonb_path_exists",
                        Boolean.class,
                        root.get("languages"),
                        criteriaBuilder.literal("$[*] ? (@ == \"" + lang + "\")")
                    )
                ))
                .toArray(jakarta.persistence.criteria.Predicate[]::new);
            return criteriaBuilder.or(predicates);
        };
    }

    public static Specification<CoachListView> hasFieldsInList(List<String> fields) {
        return (root, query, criteriaBuilder) -> {
            query.distinct(true);
            var predicates = fields.stream()
                .map(field -> criteriaBuilder.isTrue(
                    criteriaBuilder.function(
                        "jsonb_path_exists",
                        Boolean.class,
                        root.get("fields"),
                        criteriaBuilder.literal("$[*] ? (@ == \"" + field + "\")")
                    )
                ))
                .toArray(jakarta.persistence.criteria.Predicate[]::new);
            return criteriaBuilder.or(predicates);
        };
    }

    public static Specification<CoachListView> hasExperienceFrom(LocalDate from) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get(EXPERIENCE_FIELD), from);
    }

    public static Specification<CoachListView> hasExperienceTo(LocalDate to) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get(EXPERIENCE_FIELD), to);
    }

    public static Specification<CoachListView> hasRateInSet(List<String> rates) {
        return (root, query, criteriaBuilder) -> root.get("rate").in(rates);
    }

    public static Specification<CoachListView> nameStartsWith(String name) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.or(
            criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), name.toLowerCase() + "%"),
            criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), name.toLowerCase() + "%")
        );
    }
}
