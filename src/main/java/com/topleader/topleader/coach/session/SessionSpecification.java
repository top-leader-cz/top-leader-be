package com.topleader.topleader.coach.session;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.criteria.Predicate;

public class SessionSpecification {

    public static Specification<CoachSessionView> withFilter(SessionFilter filter, String coach) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("coachUsername"), coach));

            if (StringUtils.isNotBlank(filter.client())) {
                predicates.add(cb.equal(root.get("client"), filter.client()));
            }

            if (filter.status() != null) {
                predicates.add(cb.equal(root.get("status"), filter.status()));
            }

            if (filter.from() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("date"), filter.from()));
            }

            if (filter.to() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("date"), filter.to()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}