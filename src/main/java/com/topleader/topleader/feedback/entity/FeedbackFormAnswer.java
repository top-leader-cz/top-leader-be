package com.topleader.topleader.feedback.entity;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;


@Data
@Accessors(chain = true)
@Table("feedback_form_answer")
public class FeedbackFormAnswer {

    @Id
    private Long id;

    private Long formId;

    private Long recipientId;

    private String questionKey;

    private String answer;

}
