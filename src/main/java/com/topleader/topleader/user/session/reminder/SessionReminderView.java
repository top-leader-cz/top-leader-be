package com.topleader.topleader.user.session.reminder;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
@ToString(of = {"username"})
public class SessionReminderView {

    @Id
    private String username;

    private String firstName;

    private String lastName;

    private String locale;

}
