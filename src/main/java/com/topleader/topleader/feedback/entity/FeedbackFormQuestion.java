package com.topleader.topleader.feedback.entity;


import com.topleader.topleader.feedback.api.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor(onConstructor_ = @PersistenceCreator)
@Table("feedback_form_question")
public class FeedbackFormQuestion {

    @Id
    private Long id;

    @Column("form_id")
    private Long feedbackFormId;

    private String questionKey;

    private QuestionType type;

    private boolean required;

}
