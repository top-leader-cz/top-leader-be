/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.user.session;

import java.time.LocalDate;
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
@Table("user_action_step")
@Accessors(chain = true)
@NoArgsConstructor
public class UserActionStep {

    @Id
    private Long id;

    private String username;

    private String label;

    private LocalDate date;

    private Boolean checked;

}
