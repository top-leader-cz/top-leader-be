package com.topleader.topleader.feedback.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.data.relational.core.mapping.Table;
import com.topleader.topleader.common.entity.BaseEntity;


@Data
@EqualsAndHashCode(callSuper=false)
@Accessors(chain = true)
@Table("feedback_form_answer")
public class FeedbackFormAnswer extends BaseEntity {
    private Long formId;

    private Long recipientId;

    private String questionKey;

    private String answer;

}
