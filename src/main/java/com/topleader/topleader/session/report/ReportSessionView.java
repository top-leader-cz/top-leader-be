package com.topleader.topleader.session.report;

import com.topleader.topleader.session.scheduled_session.ScheduledSession;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

import static jakarta.persistence.EnumType.STRING;

@Entity
@Data
@Accessors(chain = true)
public class ReportSessionView {

    @Id
    private Long id;

    private String username;

    private String firstName;

    private String lastName;

    @Enumerated(value = STRING)
    private ScheduledSession.Status status;

    private long companyId;

    private LocalDateTime date;

}
