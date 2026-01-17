/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.user.userinfo;

import com.topleader.topleader.common.util.common.JsonUtils;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;


/**
 * @author Daniel Slavik
 */
@Getter
@Setter
@ToString
@Table("user_info")
@Accessors(chain = true)
@NoArgsConstructor
public class UserInfo {

    @Id
    private Long id;

    private String username;

    private String strengths;

    private String values;

    private String areaOfDevelopment;

    private String notes;

    private String longTermGoal;

    private String motivation;

    private String lastReflection;

    public List<String> getStrengths() {
        return strengths != null ? JsonUtils.fromJsonStringToList(strengths) : new ArrayList<>();
    }

    public UserInfo setStrengths(List<String> strengths) {
        this.strengths = strengths != null ? JsonUtils.toJsonString(strengths) : null;
        return this;
    }

    public List<String> getValues() {
        return values != null ? JsonUtils.fromJsonStringToList(values) : new ArrayList<>();
    }

    public UserInfo setValues(List<String> values) {
        this.values = values != null ? JsonUtils.toJsonString(values) : null;
        return this;
    }

    public List<String> getAreaOfDevelopment() {
        return areaOfDevelopment != null ? JsonUtils.fromJsonStringToList(areaOfDevelopment) : new ArrayList<>();
    }

    public UserInfo setAreaOfDevelopment(List<String> areaOfDevelopment) {
        this.areaOfDevelopment = areaOfDevelopment != null ? JsonUtils.toJsonString(areaOfDevelopment) : null;
        return this;
    }

    public List<String> getTopStrengths() {
        var strengthsList = getStrengths();
        var strengthSize = strengthsList.size();
        return strengthsList.subList(0, Math.min(strengthSize, 5));
    }

}
