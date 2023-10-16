package com.topleader.topleader.feedback.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Entity
@Accessors(chain = true)
public class FeedbackFormQuestion {

    @EmbeddedId
    private FeedbackFormQuestionId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("formId")
    private FeedbackForm feedbackForm;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("questionKey")
    private Question question;

    private boolean required;
    @Data
    @Embeddable
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FeedbackFormQuestionId implements Serializable {

        @Column(name = "feedback_form_id")
        private Long formId;

        @Column(name = "question_key")
        private String questionKey;
    }
}