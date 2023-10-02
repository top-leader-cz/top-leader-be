/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.scheduled_session;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import java.time.LocalDateTime;
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
public class ScheduledSession {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "scheduled_session_id_seq")
    @SequenceGenerator(name = "scheduled_session_id_seq", sequenceName = "scheduled_session_id_seq", allocationSize = 1)
    private Long id;

    private String username;

    private String coachUsername;

    private LocalDateTime time;

}
