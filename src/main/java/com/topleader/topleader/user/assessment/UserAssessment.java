/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.user.assessment;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.data.relational.core.mapping.Table;
import com.topleader.topleader.common.entity.BaseEntity;


/**
 * @author Daniel Slavik
 */
@Getter
@Setter
@ToString
@Table("user_assessment")
@Accessors(chain = true)
@NoArgsConstructor
public class UserAssessment extends BaseEntity {
    private String username;

    private Long questionId;

    private Integer answer;
}
