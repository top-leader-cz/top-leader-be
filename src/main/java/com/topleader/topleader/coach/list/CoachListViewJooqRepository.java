/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach.list;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.topleader.topleader.common.jooq.JooqFilterBuilder;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.jooq.impl.DSL;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.topleader.topleader.common.util.common.JsonUtils.MAPPER;
import static com.topleader.topleader.jooq.tables.CoachListView.COACH_LIST_VIEW;

@Repository
@RequiredArgsConstructor
public class CoachListViewJooqRepository {

    private final DSLContext dsl;

    public Page<CoachListView> findAllWithFilters(CoachListController.FilterRequest filter, Set<String> allowedRates, Pageable pageable) {
        var condition = buildCondition(filter, allowedRates);

        var total = dsl.selectCount()
                .from(COACH_LIST_VIEW)
                .where(condition)
                .fetchOne(0, Long.class);

        var records = dsl.selectFrom(COACH_LIST_VIEW)
                .where(condition)
                .orderBy(COACH_LIST_VIEW.PRIORITY.desc(), COACH_LIST_VIEW.USERNAME.asc())
                .limit(pageable.getPageSize())
                .offset((int) pageable.getOffset())
                .fetch();

        var content = records.stream()
                .map(this::toCoachListView)
                .toList();

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }

    public Optional<CoachListView> findById(String username) {
        var record = dsl.selectFrom(COACH_LIST_VIEW)
                .where(COACH_LIST_VIEW.USERNAME.eq(username))
                .fetchOne();
        return Optional.ofNullable(record).map(this::toCoachListView);
    }

    private Condition buildCondition(CoachListController.FilterRequest filter, Set<String> allowedRates) {
        var conditions = new java.util.ArrayList<Condition>();

        // Always filter by public profile
        conditions.add(COACH_LIST_VIEW.PUBLIC_PROFILE.isTrue());

        // Filter by allowed rates (from user, not from filter)
        if (allowedRates != null && !allowedRates.isEmpty()) {
            conditions.add(COACH_LIST_VIEW.RATE.in(allowedRates));
        }

        // Build conditions from filter DTO
        if (filter != null) {
            var filterCondition = JooqFilterBuilder.build(filter, COACH_LIST_VIEW)
                    .exclude("page", "prices")
                    .custom("languages", v -> {
                        @SuppressWarnings("unchecked")
                        var languages = (List<String>) v;
                        return DSL.condition("{0} ??| {1}",
                                COACH_LIST_VIEW.LANGUAGES,
                                DSL.array(languages.toArray(new String[0])));
                    })
                    .custom("fields", v -> {
                        @SuppressWarnings("unchecked")
                        var fields = (List<String>) v;
                        return DSL.condition("{0} ??| {1}",
                                COACH_LIST_VIEW.FIELDS,
                                DSL.array(fields.toArray(new String[0])));
                    })
                    .custom("experienceFrom", v -> {
                        var years = (Integer) v;
                        return COACH_LIST_VIEW.EXPERIENCE_SINCE.le(toDate(years));
                    })
                    .custom("experienceTo", v -> {
                        var years = (Integer) v;
                        return COACH_LIST_VIEW.EXPERIENCE_SINCE.ge(toDate(years));
                    })
                    .custom("name", v -> {
                        var name = (String) v;
                        var nameLike = name.toLowerCase() + "%";
                        return DSL.or(
                                DSL.lower(COACH_LIST_VIEW.FIRST_NAME).like(nameLike),
                                DSL.lower(COACH_LIST_VIEW.LAST_NAME).like(nameLike)
                        );
                    })
                    .toCondition();
            conditions.add(filterCondition);
        }

        return DSL.and(conditions);
    }

    private static LocalDate toDate(Integer years) {
        return LocalDate.now()
                .withMonth(1)
                .withDayOfMonth(1)
                .minusYears(years);
    }

    private CoachListView toCoachListView(com.topleader.topleader.jooq.tables.records.CoachListViewRecord record) {
        return new CoachListView()
                .setUsername(record.getUsername())
                .setPublicProfile(record.getPublicProfile())
                .setFirstName(record.getFirstName())
                .setLastName(record.getLastName())
                .setEmail(record.getEmail())
                .setWebLink(record.getWebLink())
                .setBio(record.getBio())
                .setTimeZone(record.getTimeZone())
                .setExperienceSince(record.getExperienceSince())
                .setRate(record.getRate())
                .setRateOrder(record.getRateOrder())
                .setLinkedinProfile(record.getLinkedinProfile())
                .setPriority(record.getPriority() != null ? record.getPriority() : 0)
                .setPrimaryRoles(parseJsonbString(record.getPrimaryRoles()))
                .setCertificate(parseJsonbString(record.getCertificate()))
                .setLanguages(parseJsonbSet(record.getLanguages()))
                .setFields(parseJsonbSet(record.getFields()));
    }

    private static String parseJsonbString(JSONB jsonb) {
        if (jsonb == null || jsonb.data() == null || jsonb.data().isBlank()) {
            return null;
        }
        return jsonb.data();
    }

    private static Set<String> parseJsonbSet(JSONB jsonb) {
        if (jsonb == null || jsonb.data() == null || jsonb.data().isBlank()) {
            return new HashSet<>();
        }
        try {
            return MAPPER.readValue(jsonb.data(), new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            return new HashSet<>();
        }
    }
}
