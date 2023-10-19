package com.topleader.topleader.feedback.entity;


import com.topleader.topleader.feedback.api.QuestionType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Entity
@Accessors(chain = true)
@ToString(of={"id", "form", "question", "type", "required"})
public class FeedbackFormQuestion {

    @EmbeddedId
    private FeedbackFormQuestionId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("formId")
    private FeedbackForm form;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("questionKey")
    private Question question;

    @Enumerated(EnumType.STRING)
    private QuestionType type;

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
