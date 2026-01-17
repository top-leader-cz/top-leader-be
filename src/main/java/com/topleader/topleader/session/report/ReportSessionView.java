package com.topleader.topleader.session.report;

import com.topleader.topleader.session.scheduled_session.ScheduledSession;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("report_session_view")
@Data
@Accessors(chain = true)
public class ReportSessionView {

    @Id
    private Long id;

    private String username;

    private String firstName;

    private String lastName;

    private String status;

    private Long companyId;

    private LocalDateTime date;

    public ScheduledSession.Status getStatusEnum() {
        return status != null ? ScheduledSession.Status.valueOf(status) : null;
    }
}
