/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.common.jooq;

import org.jooq.SortField;
import org.jooq.Table;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility to convert Spring Data Sort to jOOQ SortFields.
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * var orderFields = JooqSortBuilder.build(pageable.getSort(), ADMIN_VIEW);
 * dsl.selectFrom(ADMIN_VIEW).orderBy(orderFields).fetch();
 * }</pre>
 */
public final class JooqSortBuilder {

    private JooqSortBuilder() {}

    public static List<SortField<?>> build(Sort sort, Table<?> table) {
        if (sort == null || sort.isUnsorted()) {
            return List.of();
        }

        var fields = new ArrayList<SortField<?>>();
        sort.forEach(order -> {
            var field = table.field(camelToSnake(order.getProperty()));
            if (field != null) {
                fields.add(order.isDescending() ? field.desc() : field.asc());
            }
        });
        return fields;
    }

    private static String camelToSnake(String str) {
        return str.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
}
