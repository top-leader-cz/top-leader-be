package com.topleader.topleader.feedback.entity;


import com.topleader.topleader.feedback.api.QuestionType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import com.topleader.topleader.common.entity.BaseEntity;


@Data
@EqualsAndHashCode(callSuper=false)
@Accessors(chain = true)
@Table("feedback_form_question")
public class FeedbackFormQuestion extends BaseEntity {
    @Column("form_id")
    private Long feedbackFormId;

    private String questionKey;

    private QuestionType type;

    private boolean required;

}
