/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.user.session;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;


/**
 * @author Daniel Slavik
 */
@Getter
@Setter
@ToString
@Entity
@Accessors(chain = true)
@NoArgsConstructor
public class UserActionStep {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_action_step_seq")
    @SequenceGenerator(name = "user_action_step_seq", sequenceName = "user_action_step_seq", allocationSize = 1)
    private Long id;

    private String username;

    private String label;

    private LocalDate date;

    private Boolean checked;

}
