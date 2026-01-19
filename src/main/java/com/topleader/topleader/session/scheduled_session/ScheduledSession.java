/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.session.scheduled_session;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import com.topleader.topleader.common.entity.BaseEntity;


/**
 * @author Daniel Slavik
 */
@Getter
@Setter
@ToString
@Table("scheduled_session")
@Accessors(chain = true)
@NoArgsConstructor
public class ScheduledSession extends BaseEntity {
    private String username;

    private String coachUsername;

    private LocalDateTime time;

    private boolean paid;

    private boolean isPrivate;

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
