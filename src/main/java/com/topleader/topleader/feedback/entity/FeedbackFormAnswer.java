package com.topleader.topleader.feedback.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Entity
@Table
public class FeedbackFormAnswer {

    @EmbeddedId
    private FeedbackFormAnswerId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("formId")
    private FeedbackForm feedbackForm;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("questionKey")
    private Question question;

    @ManyToOne( fetch = FetchType.LAZY)
    @MapsId("answerKey")
    private Answer answer;

    private String recipient;


    @Data
    @Embeddable
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FeedbackFormAnswerId implements Serializable {

        @Column(name = "feedback_form_id")
        private Long formId;

        @Column(name = "question_key")
        private String questionKey;

        @Column(name = "answer_key")
        private String answerKey;
    }
}
