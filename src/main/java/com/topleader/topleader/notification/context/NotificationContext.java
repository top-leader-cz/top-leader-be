/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.notification.context;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import static com.topleader.topleader.notification.context.NotificationContext.COACH_LINKED_CONTEXT;
import static com.topleader.topleader.notification.context.NotificationContext.COACH_UNLINKED_CONTEXT;
import static com.topleader.topleader.notification.context.NotificationContext.MESSAGE_CONTEXT;


/**
 * @author Daniel Slavik
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = MessageNotificationContext.class, name = MESSAGE_CONTEXT),
    @JsonSubTypes.Type(value = CoachUnlinkedNotificationContext.class, name = COACH_UNLINKED_CONTEXT),
    @JsonSubTypes.Type(value = CoachLinkedNotificationContext.class, name = COACH_LINKED_CONTEXT),
})
public interface NotificationContext {
    String MESSAGE_CONTEXT = "MESSAGE";
    String COACH_UNLINKED_CONTEXT = "COACH_UNLINKED";
    String COACH_LINKED_CONTEXT = "COACH_LINKED";
}
