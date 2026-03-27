package com.topleader.topleader.program.participant.assessment;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Accessors(chain = true)
@Table("assessment_question")
public class AssessmentQuestion {

    @Id
    private Long id;

    private String focusAreaKey;

    private int questionOrder;

    private String questionText;
}
