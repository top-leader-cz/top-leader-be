/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach.list;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.topleader.topleader.coach.Coach;
import com.topleader.topleader.common.jooq.JooqPageBuilder;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.jooq.impl.DSL;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.topleader.topleader.common.util.common.JsonUtils.MAPPER;
import static com.topleader.topleader.jooq.tables.CoachFields.COACH_FIELDS;
import static com.topleader.topleader.jooq.tables.CoachLanguages.COACH_LANGUAGES;
import static com.topleader.topleader.jooq.tables.CoachListView.COACH_LIST_VIEW;

@Repository
@RequiredArgsConstructor
public class CoachListViewJooqRepository {

    private final DSLContext dsl;

    public Page<CoachListView> findAllWithFilters(CoachListController.FilterRequest filter, Set<String> allowedRates, Pageable pageable) {
        return JooqPageBuilder.from(dsl, COACH_LIST_VIEW, pageable)
                .where(buildCondition(filter, allowedRates))
                .map(this::toCoachListView)
                .fetch();
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

        // Filter by languages using EXISTS subquery
        if (filter != null && filter.languages() != null && !filter.languages().isEmpty()) {
            conditions.add(DSL.exists(
                    DSL.selectOne()
                            .from(COACH_LANGUAGES)
                            .where(COACH_LANGUAGES.COACH_USERNAME.eq(COACH_LIST_VIEW.USERNAME))
                            .and(COACH_LANGUAGES.LANGUAGES.in(filter.languages()))
            ));
        }

        // Filter by fields using EXISTS subquery
        if (filter != null && filter.fields() != null && !filter.fields().isEmpty()) {
            conditions.add(DSL.exists(
                    DSL.selectOne()
                            .from(COACH_FIELDS)
                            .where(COACH_FIELDS.COACH_USERNAME.eq(COACH_LIST_VIEW.USERNAME))
                            .and(COACH_FIELDS.FIELDS.in(filter.fields()))
            ));
        }

        // Filter by experience from (coaches with experience since <= date)
        if (filter != null && filter.experienceFrom() != null) {
            var experienceFromDate = toDate(filter.experienceFrom());
            conditions.add(COACH_LIST_VIEW.EXPERIENCE_SINCE.le(experienceFromDate));
        }

        // Filter by experience to (coaches with experience since >= date)
        if (filter != null && filter.experienceTo() != null) {
            var experienceToDate = toDate(filter.experienceTo());
            conditions.add(COACH_LIST_VIEW.EXPERIENCE_SINCE.ge(experienceToDate));
        }

        // Filter by rates
        if (allowedRates != null && !allowedRates.isEmpty()) {
            conditions.add(COACH_LIST_VIEW.RATE.in(allowedRates));
        }

        // Filter by name (first name or last name starts with)
        if (filter != null && filter.name() != null && !filter.name().isBlank()) {
            var nameLike = filter.name().toLowerCase() + "%";
            conditions.add(DSL.or(
                    DSL.lower(COACH_LIST_VIEW.FIRST_NAME).like(nameLike),
                    DSL.lower(COACH_LIST_VIEW.LAST_NAME).like(nameLike)
            ));
        }

        return conditions.isEmpty() ? DSL.trueCondition() : DSL.and(conditions);
    }

    private static LocalDate toDate(Integer years) {
        return LocalDate.now()
                .withMonth(1)
                .withDayOfMonth(1)
                .minusYears(years);
    }

    private CoachListView toCoachListView(com.topleader.topleader.jooq.tables.records.CoachListViewRecord record) {
        var view = new CoachListView()
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
                .setPriority(record.getPriority() != null ? record.getPriority() : 0);

        // Parse JSON fields
        view.setPrimaryRoles(parseJsonb(record.getPrimaryRoles()));
        view.setCertificate(parseJsonbSet(record.getCertificate()));

        // Load languages and fields
        view.setLanguages(loadLanguages(record.getUsername()));
        view.setFields(loadFields(record.getUsername()));

        return view;
    }

    private Set<String> loadLanguages(String username) {
        return new HashSet<>(dsl.select(COACH_LANGUAGES.LANGUAGES)
                .from(COACH_LANGUAGES)
                .where(COACH_LANGUAGES.COACH_USERNAME.eq(username))
                .fetch(COACH_LANGUAGES.LANGUAGES));
    }

    private Set<String> loadFields(String username) {
        return new HashSet<>(dsl.select(COACH_FIELDS.FIELDS)
                .from(COACH_FIELDS)
                .where(COACH_FIELDS.COACH_USERNAME.eq(username))
                .fetch(COACH_FIELDS.FIELDS));
    }

    private static String parseJsonb(JSONB jsonb) {
        if (jsonb == null || jsonb.data() == null || jsonb.data().isBlank()) {
            return null;
        }
        return jsonb.data();
    }

    private static String parseJsonbSet(JSONB jsonb) {
        if (jsonb == null || jsonb.data() == null || jsonb.data().isBlank()) {
            return null;
        }
        return jsonb.data();
    }
}
