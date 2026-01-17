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

    private String status = Status.UPCOMING.name();

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String updatedBy;

    public Status getStatusEnum() {
        return status != null ? Status.valueOf(status) : null;
    }

    public ScheduledSession setStatusEnum(Status status) {
        this.status = status != null ? status.name() : null;
        return this;
    }

    public ScheduledSession setStatus(Status status) {
        return setStatusEnum(status);
    }

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
