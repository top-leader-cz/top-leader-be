package com.topleader.topleader.common.notification.context;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import static com.topleader.topleader.common.notification.context.NotificationContext.COACH_LINKED_CONTEXT;
import static com.topleader.topleader.common.notification.context.NotificationContext.COACH_UNLINKED_CONTEXT;
import static com.topleader.topleader.common.notification.context.NotificationContext.MEET_LINK_FAILED_CONTEXT;
import static com.topleader.topleader.common.notification.context.NotificationContext.MESSAGE_CONTEXT;


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
    @JsonSubTypes.Type(value = MeetLinkFailedNotificationContext.class, name = MEET_LINK_FAILED_CONTEXT),
})
public interface NotificationContext {
    String MESSAGE_CONTEXT = "MESSAGE";
    String COACH_UNLINKED_CONTEXT = "COACH_UNLINKED";
    String COACH_LINKED_CONTEXT = "COACH_LINKED";
    String MEET_LINK_FAILED_CONTEXT = "MEET_LINK_FAILED";
}
