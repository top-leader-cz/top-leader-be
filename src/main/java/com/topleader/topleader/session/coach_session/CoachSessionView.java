package com.topleader.topleader.session.coach_session;

import com.topleader.topleader.session.scheduled_session.ScheduledSession;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.ZonedDateTime;

@Table("coach_session_view")
@Data
public class CoachSessionView {

    @Id
    private Long id;

    private String coachUsername;

    private ZonedDateTime date;

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
