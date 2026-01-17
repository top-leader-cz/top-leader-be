/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.topleader.topleader.common.jooq.JooqFilterBuilder;
import com.topleader.topleader.common.jooq.JooqPageBuilder;
import com.topleader.topleader.user.User;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

import static com.topleader.topleader.common.util.common.JsonUtils.MAPPER;
import static com.topleader.topleader.jooq.tables.AdminView.ADMIN_VIEW;
import static com.topleader.topleader.user.User.Status.*;

@Repository
@RequiredArgsConstructor
public class AdminViewRepository {

    private final DSLContext dsl;

    public Page<AdminView> findAllWithFilters(AdminViewController.FilterDto filter, Pageable pageable) {
        return JooqPageBuilder.from(dsl, ADMIN_VIEW, pageable)
                .where(buildCondition(filter))
                .map(this::toAdminView)
                .fetch();
    }

    private org.jooq.Condition buildCondition(AdminViewController.FilterDto filter) {
        return JooqFilterBuilder.build(filter, ADMIN_VIEW)
                .likeIgnoreCase("firstName", "companyName", "coach", "coachFirstName", "coachLastName", "hrs", "requestedBy")
                .exclude("isTrial", "maxCoachRate")
                .custom("showCanceled", v -> {
                    var show = (Boolean) v;
                    var statuses = show
                            ? List.of(AUTHORIZED.name(), PENDING.name(), PAID.name(), REQUESTED.name(), VIEWED.name(), SUBMITTED.name(), CANCELED.name())
                            : List.of(AUTHORIZED.name(), PENDING.name(), PAID.name(), REQUESTED.name(), VIEWED.name(), SUBMITTED.name());
                    return ADMIN_VIEW.STATUS.in(statuses);
                })
                .toCondition();
    }

    private AdminView toAdminView(com.topleader.topleader.jooq.tables.records.AdminViewRecord record) {
        return new AdminView()
                .setUsername(record.getUsername())
                .setFirstName(record.getFirstName())
                .setLastName(record.getLastName())
                .setAuthorities(parseAuthorities(record.getAuthorities()))
                .setTimeZone(record.getTimeZone())
                .setStatus(parseStatus(record.getStatus()))
                .setCompanyId(record.getCompanyId())
                .setCompanyName(record.getCompanyName())
                .setCoach(record.getCoach())
                .setCoachFirstName(record.getCoachFirstName())
                .setCoachLastName(record.getCoachLastName())
                .setCredit(record.getCredit())
                .setRequestedCredit(record.getRequestedCredit())
                .setSumRequestedCredit(record.getSumRequestedCredit())
                .setPaidCredit(record.getPaidCredit())
                .setScheduledCredit(record.getScheduledCredit())
                .setHrs(record.getHrs())
                .setRequestedBy(record.getRequestedBy())
                .setFreeCoach(record.getFreeCoach())
                .setLocale(record.getLocale())
                .setAllowedCoachRates(record.getAllowedCoachRates())
                .setRate(record.getRate())
                .setCertificate(parseJsonb(record.getCertificate(), new TypeReference<>() {}))
                .setInternalRate(record.getInternalRate())
                .setPrimaryRoles(parseJsonb(record.getPrimaryRoles(), new TypeReference<>() {}));
    }

    private static Set<User.Authority> parseAuthorities(String json) {
        if (json == null || json.isBlank()) {
            return Set.of(User.Authority.USER);
        }
        try {
            return MAPPER.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            return Set.of(User.Authority.USER);
        }
    }

    private static User.Status parseStatus(String status) {
        if (status == null) {
            return null;
        }
        return User.Status.valueOf(status);
    }

    private static <T> T parseJsonb(JSONB jsonb, TypeReference<T> typeRef) {
        if (jsonb == null || jsonb.data().isBlank()) {
            return null;
        }
        try {
            return MAPPER.readValue(jsonb.data(), typeRef);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
