package com.topleader.topleader.program.participant.assessment;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@Table("assessment_response")
public class AssessmentResponse {

    public enum Type {
        BASELINE, MID, FINAL
    }

    @Id
    private Long id;

    private Long participantId;

    private Type type;

    private int cycle;

    private String focusAreaKey;

    private Integer q1;
    private Integer q2;
    private Integer q3;
    private Integer q4;
    private Integer q5;

    private String openText;

    private Integer nps;

    private LocalDateTime completedAt;
}
