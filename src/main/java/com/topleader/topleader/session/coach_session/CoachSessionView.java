package com.topleader.topleader.session.coach_session;

import com.topleader.topleader.session.scheduled_session.ScheduledSession;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.ZonedDateTime;

@Entity
@Data
public class CoachSessionView {

    @Id
    private Long id;

    private String coachUsername;

    private ZonedDateTime date;

    @Enumerated(EnumType.STRING)
    private ScheduledSession.Status status;

    private String client;

    private String firstName;

    private String lastName;

    public  CoachSessionViewDto toDto() {
        return new CoachSessionViewDto(id, date, status, client, firstName, lastName);
    }

    public record CoachSessionViewDto(Long id, ZonedDateTime date, ScheduledSession.Status status, String client, String firstName, String lastName) {

    }
}
