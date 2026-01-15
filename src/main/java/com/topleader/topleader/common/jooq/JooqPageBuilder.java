/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.common.jooq;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.function.Function;

/**
 * Utility for executing paginated jOOQ queries.
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * return JooqPageBuilder.from(dsl, ADMIN_VIEW, pageable)
 *     .where(condition)
 *     .map(this::toAdminView)
 *     .fetch();
 * }</pre>
 */
public class JooqPageBuilder<R extends Record, T> {

    private final DSLContext dsl;
    private final Table<R> table;
    private final Pageable pageable;
    private Condition condition = DSL.trueCondition();
    private Function<R, T> mapper;

    private JooqPageBuilder(DSLContext dsl, Table<R> table, Pageable pageable) {
        this.dsl = dsl;
        this.table = table;
        this.pageable = pageable;
    }

    public static <R extends Record> JooqPageBuilder<R, R> from(DSLContext dsl, Table<R> table, Pageable pageable) {
        return new JooqPageBuilder<R, R>(dsl, table, pageable).map(Function.identity());
    }

    public JooqPageBuilder<R, T> where(Condition condition) {
        this.condition = condition != null ? condition : DSL.trueCondition();
        return this;
    }

    @SuppressWarnings("unchecked")
    public <U> JooqPageBuilder<R, U> map(Function<R, U> mapper) {
        var builder = (JooqPageBuilder<R, U>) this;
        builder.mapper = mapper;
        return builder;
    }

    public Page<T> fetch() {
        var total = dsl.selectCount()
                .from(table)
                .where(condition)
                .fetchOne(0, Long.class);

        var orderFields = JooqSortBuilder.build(pageable.getSort(), table);

        var records = dsl.selectFrom(table)
                .where(condition)
                .orderBy(orderFields)
                .limit(pageable.getPageSize())
                .offset((int) pageable.getOffset())
                .fetch();

        var content = records.stream()
                .map(mapper)
                .toList();

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }
}
