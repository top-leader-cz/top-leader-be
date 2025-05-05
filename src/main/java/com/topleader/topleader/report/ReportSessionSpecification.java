package com.topleader.topleader.report;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ReportSessionSpecification {

    public static Specification<ReportSessionView> withFilter(ReportSessionFilter filter, long companyId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("companyId"), companyId));

            if (filter.from() != null) {
                predicates.add(cb.or(cb.greaterThanOrEqualTo(root.get("date"), filter.from()), cb.isNull(root.get("date"))));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
