package com.topleader.topleader.hr.program;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@Table("program")
public class Program {

    @Id
    private Long id;

    private Long coachingPackageId;

    private String name;

    private String goal;

    private String targetGroup;

    private Status status = Status.DRAFT;

    private Integer durationDays;

    private Integer cycleLengthDays;

    private Set<String> focusAreas = Set.of();

    private Integer sessionsPerParticipant;

    private String recommendedCadence;

    private CoachAssignmentModel coachAssignmentModel = CoachAssignmentModel.PARTICIPANT_CHOOSES;

    private Set<String> shortlistedCoaches = Set.of();

    private boolean microActionsEnabled = true;

    private Set<String> enabledOptions = Set.of();

    private LocalDateTime milestoneDate;

    private LocalDateTime createdAt = LocalDateTime.now();

    private String createdBy;

    private LocalDateTime updatedAt = LocalDateTime.now();

    private String updatedBy;

    public enum Status {
        DRAFT,
        CREATED,
        ACTIVE,
        COMPLETED
    }

    public enum CoachAssignmentModel {
        PARTICIPANT_CHOOSES,
        HR_ASSIGNS,
        HR_SHORTLIST
    }
}
