package com.topleader.topleader.hr.program.participant;

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

    private LocalDateTime createdAt;

    private String createdBy;
}
