/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.common.jooq;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.util.StringUtils;

import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Generic jOOQ filter builder that creates conditions from any filter DTO.
 * Supports String, Integer, Long, Boolean, Enum types and collections.
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * var condition = JooqFilterBuilder.build(filter, ADMIN_VIEW)
 *     .likeIgnoreCase("firstName", "lastName", "companyName")
 *     .custom("showCanceled", (value) -> buildStatusCondition((Boolean) value))
 *     .exclude("showCanceled")
 *     .toCondition();
 * }</pre>
 */
public class JooqFilterBuilder<R extends org.jooq.Record> {

    private final Object filter;
    private final Table<R> table;
    private final List<String> likeIgnoreCaseFields = new ArrayList<>();
    private final List<String> likeFields = new ArrayList<>();
    private final List<String> excludedFields = new ArrayList<>();
    private final List<CustomCondition> customConditions = new ArrayList<>();

    private JooqFilterBuilder(Object filter, Table<R> table) {
        this.filter = filter;
        this.table = table;
    }

    public static <R extends org.jooq.Record> JooqFilterBuilder<R> build(Object filter, Table<R> table) {
        return new JooqFilterBuilder<>(filter, table);
    }

    /**
     * Mark fields to use LIKE %value% with case-insensitive matching
     */
    public JooqFilterBuilder<R> likeIgnoreCase(String... fieldNames) {
        likeIgnoreCaseFields.addAll(List.of(fieldNames));
        return this;
    }

    /**
     * Mark fields to use LIKE %value% with case-sensitive matching
     */
    public JooqFilterBuilder<R> like(String... fieldNames) {
        likeFields.addAll(List.of(fieldNames));
        return this;
    }

    /**
     * Exclude fields from automatic processing (use with custom())
     */
    public JooqFilterBuilder<R> exclude(String... fieldNames) {
        excludedFields.addAll(List.of(fieldNames));
        return this;
    }

    /**
     * Add custom condition for a field
     */
    public JooqFilterBuilder<R> custom(String fieldName, Function<Object, Condition> conditionBuilder) {
        customConditions.add(new CustomCondition(fieldName, conditionBuilder));
        excludedFields.add(fieldName);
        return this;
    }

    /**
     * Build the final jOOQ Condition
     */
    public Condition toCondition() {
        if (filter == null) {
            return DSL.trueCondition();
        }

        var customConditionsList = customConditions.stream()
                .map(custom -> Optional.ofNullable(getFieldValue(custom.fieldName()))
                        .map(custom.builder())
                        .orElse(null))
                .filter(Objects::nonNull);

        var fieldNames = filter.getClass().isRecord()
                ? Arrays.stream(filter.getClass().getRecordComponents()).map(RecordComponent::getName)
                : Arrays.stream(filter.getClass().getDeclaredFields()).map(java.lang.reflect.Field::getName);

        var fieldConditions = fieldNames
                .map(this::processField)
                .filter(Objects::nonNull);

        var conditions = Stream.concat(customConditionsList, fieldConditions).toList();

        return conditions.isEmpty() ? DSL.trueCondition() : DSL.and(conditions);
    }

    private Condition processField(String fieldName) {
        if (excludedFields.contains(fieldName)) {
            return null;
        }

        var value = getFieldValue(fieldName);
        if (value == null) {
            return null;
        }

        // Skip empty strings
        if (value instanceof String str && !StringUtils.hasText(str)) {
            return null;
        }

        // Skip empty collections
        if (value instanceof Collection<?> col && col.isEmpty()) {
            return null;
        }

        var column = table.field(camelToSnake(fieldName));

        return column != null ? buildCondition(fieldName, value, column) : null;
    }

    @SuppressWarnings("unchecked")
    private Condition buildCondition(String fieldName, Object value, Field<?> column) {
        // String handling
        if (value instanceof String str) {
            var stringColumn = (Field<String>) column;
            if (likeIgnoreCaseFields.contains(fieldName)) {
                return stringColumn.likeIgnoreCase("%" + str + "%");
            } else if (likeFields.contains(fieldName)) {
                return stringColumn.like("%" + str + "%");
            } else {
                return stringColumn.eq(str);
            }
        }

        // Enum handling - convert to string name
        if (value instanceof Enum<?> enumValue) {
            var stringColumn = (Field<String>) column;
            return stringColumn.eq(enumValue.name());
        }

        // Integer handling
        if (value instanceof Integer intValue) {
            var intColumn = (Field<Integer>) column;
            return intColumn.eq(intValue);
        }

        // Long handling
        if (value instanceof Long longValue) {
            var longColumn = (Field<Long>) column;
            return longColumn.eq(longValue);
        }

        // Boolean handling
        if (value instanceof Boolean boolValue) {
            var boolColumn = (Field<Boolean>) column;
            return boolColumn.eq(boolValue);
        }

        // Collection of strings
        if (value instanceof Collection<?> collection && !collection.isEmpty()) {
            var first = collection.iterator().next();
            if (first instanceof String) {
                var stringColumn = (Field<String>) column;
                return stringColumn.in((Collection<String>) collection);
            }
            if (first instanceof Enum<?>) {
                var stringColumn = (Field<String>) column;
                var names = ((Collection<Enum<?>>) collection).stream()
                        .map(Enum::name)
                        .toList();
                return stringColumn.in(names);
            }
        }

        return null;
    }

    private Object getFieldValue(String fieldName) {
        try {
            if (filter.getClass().isRecord()) {
                for (RecordComponent component : filter.getClass().getRecordComponents()) {
                    if (component.getName().equals(fieldName)) {
                        return component.getAccessor().invoke(filter);
                    }
                }
            } else {
                var field = filter.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(filter);
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    private static String camelToSnake(String str) {
        return str.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    private record CustomCondition(String fieldName, Function<Object, Condition> builder) {}
}
