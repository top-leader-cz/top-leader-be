package com.topleader.topleader.hr.program;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@Table("program_participant")
public class ProgramParticipant {

    @Id
    private Long id;

    private Long programId;

    private String username;

    private String coachUsername;

    private Status status = Status.ON_TRACK;

    private LocalDateTime createdAt;

    private String createdBy;

    public enum Status {
        ON_TRACK,
        AT_RISK,
        ON_HOLD
    }
}
