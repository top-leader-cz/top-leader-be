/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.common.notification;

import com.topleader.topleader.common.notification.context.NotificationContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;


@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor(onConstructor_ = @PersistenceCreator)
@Table("notification")
public class Notification {

    @Id
    private Long id;

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
