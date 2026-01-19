/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.common.notification;

import com.topleader.topleader.common.notification.context.NotificationContext;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import com.topleader.topleader.common.entity.BaseEntity;


@Data
@EqualsAndHashCode(callSuper=false)
@Accessors(chain = true)
@Table("notification")
public class Notification extends BaseEntity {
    private String username;

    private Type type;

    private boolean read;

    private NotificationContext context;

    private LocalDateTime createdAt;

    public enum Type {
        MESSAGE,
        COACH_UNLINKED,
        COACH_LINKED
    }
}
