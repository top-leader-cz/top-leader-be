/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.notification.context;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

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
})
public interface NotificationContext {
    String MESSAGE_CONTEXT = "MESSAGE";
}
