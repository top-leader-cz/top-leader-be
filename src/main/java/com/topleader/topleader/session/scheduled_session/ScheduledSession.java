/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.session.scheduled_session;

import java.time.LocalDateTime;
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
@Accessors(chain = true)
@NoArgsConstructor
@Table("scheduled_session")
public class ScheduledSession {

    @Id
    private Long id;

    private String username;

    private String coachUsername;

    private LocalDateTime time;

    private boolean paid;

    private boolean isPrivate;

    @org.springframework.data.relational.core.mapping.Column("status")
    private Status status = Status.UPCOMING;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String updatedBy;

    public enum Status {
        COMPLETED,
        UPCOMING,
        CANCELED,
        CANCELED_BY_COACH,
        CANCELED_BY_CLIENT,
        PENDING,
        NO_SHOW_CLIENT,
    }
}
