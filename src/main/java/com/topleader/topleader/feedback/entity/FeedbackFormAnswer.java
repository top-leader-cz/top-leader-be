package com.topleader.topleader.feedback.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Entity
@Table
@Accessors(chain = true)
@EqualsAndHashCode(of = {"id"})
@ToString(of={"id", "answer"})
public class FeedbackFormAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "form_id")
    private Long formId;

    @Column(name = "recipient_id")
    private Long recipientId;

    @Column(name = "question_key")
    private String questionKey;

    @ManyToOne
    @JoinColumn(name = "form_id", insertable = false, updatable = false)
    private FeedbackForm form;

    @ManyToOne
    @JoinColumn(name = "question_key", insertable = false, updatable = false)
    private Question question;

    @ManyToOne(cascade = { CascadeType.MERGE})
    @JoinColumn(name = "recipient_id", insertable = false, updatable = false)
    private Recipient recipient;

    private String answer;
}
