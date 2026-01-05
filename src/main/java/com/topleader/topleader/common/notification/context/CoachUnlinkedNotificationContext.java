/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.common.notification.context;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.experimental.Accessors;


/**
 * @author Daniel Slavik
 */
@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeName(NotificationContext.COACH_UNLINKED_CONTEXT)
public class CoachUnlinkedNotificationContext implements NotificationContext {
    private String coach;
}
