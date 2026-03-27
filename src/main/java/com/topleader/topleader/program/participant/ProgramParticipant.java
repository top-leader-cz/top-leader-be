package com.topleader.topleader.program.participant;

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

    public enum Status {
        INVITED, ENROLLING, ACTIVE, AT_RISK, COMPLETED
    }

    @Id
    private Long id;

    private Long programId;

    private String username;

    private String coachUsername;

    private String managerUsername;

    private Status status = Status.INVITED;

    private String focusArea;

    private String personalGoal;

    private int currentCycle = 1;

    private LocalDateTime enrolledAt;

    private LocalDateTime createdAt;

    private String createdBy;
}
