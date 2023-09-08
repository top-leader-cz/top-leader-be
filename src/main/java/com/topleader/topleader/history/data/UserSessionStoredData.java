/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.history.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;


/**
 * @author Daniel Slavik
 */
@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeName(StoredData.USER_SESSION_TYPE)
public class UserSessionStoredData implements StoredData {

    List<String> areaOfDevelopment;
    String longTermGoal;
    String motivation;
    String reflection;
    List<ActionStepData> actionSteps;

    public record ActionStepData(Long id, String label, LocalDate date, Boolean checked) {}
}
