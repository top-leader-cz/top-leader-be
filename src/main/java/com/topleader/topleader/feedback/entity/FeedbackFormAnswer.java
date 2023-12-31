package com.topleader.topleader.feedback.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Entity
@Table
@Accessors(chain = true)
@ToString(of={"id", "form.id", "question.key", "recipient.recipient", "answer"})
public class FeedbackFormAnswer {

    @EmbeddedId
    private FeedbackFormAnswerId id;

    @ManyToOne
    @MapsId("formId")
    private FeedbackForm form;

    @ManyToOne
    @MapsId("questionKey")
    private Question question;

    @ManyToOne(cascade = { CascadeType.MERGE})
    @MapsId("recipientId")
    private Recipient recipient;

    private String answer;


    @Data
    @Embeddable
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FeedbackFormAnswerId implements Serializable {

        @Column(name = "form_id")
        private Long formId;

        @Column(name = "recipient_id")
        private Long recipientId;

        @Column(name = "question_key")
        private String questionKey;


    }
}
