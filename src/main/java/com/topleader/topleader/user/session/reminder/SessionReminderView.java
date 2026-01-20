package com.topleader.topleader.user.session.reminder;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Table("session_reminder_view")
@ToString(of = {"username"})
public class SessionReminderView {

    @Id
    private String username;

    private String email;

    private String firstName;

    private String lastName;

    private String locale;

    private LocalDateTime time;

    private ReminderInterval reminderInterval;

    public enum ReminderInterval {
        DAYS3, DAYS10, DAYS24
    }
}
