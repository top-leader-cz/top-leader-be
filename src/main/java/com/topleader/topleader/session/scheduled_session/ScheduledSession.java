/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.session.scheduled_session;

import jakarta.persistence.*;

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

    private boolean paid;

    private boolean isPrivate;

    @Enumerated(EnumType.STRING)
    private Status status = Status.UPCOMING;

    public enum Status {
        COMPLETED,
        UPCOMING,
        CANCELED,
        PENDING,
        NO_SHOW_CLIENT,
    }

}
