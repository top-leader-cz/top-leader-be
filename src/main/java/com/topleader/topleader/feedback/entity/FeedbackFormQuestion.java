package com.topleader.topleader.feedback.entity;


import com.topleader.topleader.feedback.api.QuestionType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Entity
@Accessors(chain = true)
@EqualsAndHashCode(of = {"id"})
@ToString(of={"id", "feedbackFormId", "questionKey", "type", "required"})
public class FeedbackFormQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "form_id")
    private Long feedbackFormId;

    @Column(name = "question_key")
    private String questionKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id", insertable = false, updatable = false)
    private FeedbackForm form;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_key", insertable = false, updatable = false)
    private Question question;

    @Enumerated(EnumType.STRING)
    private QuestionType type;

    private boolean required;
}
