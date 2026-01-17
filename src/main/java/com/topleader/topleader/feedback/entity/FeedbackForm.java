package com.topleader.topleader.feedback.entity;


import com.topleader.topleader.feedback.api.Summary;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;


@Data
@Accessors(chain = true)
@Table("feedback_form")
public class FeedbackForm {

    @Id
    private Long id;

    private String title;

    private String description;

    private LocalDateTime validTo;

    private LocalDateTime createdAt;

    private boolean draft;

    private Summary summary;

    private String username;

}
