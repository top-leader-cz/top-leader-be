package com.topleader.topleader.common.notification.context;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeName(NotificationContext.MEET_LINK_FAILED_CONTEXT)
public class MeetLinkFailedNotificationContext implements NotificationContext {

    private String clientName;

    private String sessionTime;
}
