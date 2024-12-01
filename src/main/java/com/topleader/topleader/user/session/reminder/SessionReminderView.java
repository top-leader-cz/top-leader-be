package com.topleader.topleader.user.session.reminder;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@ToString(of = {"username"})
public class SessionReminderView {

    @Id
    private String username;

    private String firstName;

    private String lastName;

    private String locale;

    private LocalDateTime time;

    @Enumerated(EnumType.STRING)
    private ReminderInterval reminderInterval;

    public enum ReminderInterval {
        DAYS3, DAYS10, DAYS24
    }
}
