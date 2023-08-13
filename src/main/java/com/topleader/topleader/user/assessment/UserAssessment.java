/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.user.assessment;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.Data;
import lombok.experimental.Accessors;


/**
 * @author Daniel Slavik
 */
@Data
@Entity
@Accessors(chain = true)
@IdClass(UserAssessmentId.class)
public class UserAssessment {

    @Id
    private String username;

    @Id
    private Long questionId;

    private Integer answer;
}
