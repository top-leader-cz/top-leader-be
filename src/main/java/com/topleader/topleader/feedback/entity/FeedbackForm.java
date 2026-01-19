package com.topleader.topleader.feedback.entity;


import com.topleader.topleader.feedback.api.Summary;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import com.topleader.topleader.common.entity.BaseEntity;


@Data
@EqualsAndHashCode(callSuper=false)
@Accessors(chain = true)
@Table("feedback_form")
public class FeedbackForm extends BaseEntity {
    private String title;

    private String description;

    private LocalDateTime validTo;

    private LocalDateTime createdAt;

    private boolean draft;

    private Summary summary;

    private String username;

}
