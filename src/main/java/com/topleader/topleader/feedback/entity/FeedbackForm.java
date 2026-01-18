package com.topleader.topleader.feedback.entity;


import com.topleader.topleader.feedback.api.Summary;
import com.topleader.topleader.feedback.api.converter.SummaryConverter;
import com.topleader.topleader.user.User;
import com.topleader.topleader.common.util.converter.SetConverter;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Data
@Entity
@Accessors(chain = true)
@EqualsAndHashCode(of = {"id"})
@ToString(of = {"id", "title", "description", "validTo"})
public class FeedbackForm {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "feedback_form_id_seq")
    @SequenceGenerator(name = "feedback_form_id_seq", sequenceName = "feedback_form_id_seq", allocationSize = 1)
    private Long id;

    private String title;

    private String description;

    private LocalDateTime validTo;

    private LocalDateTime createdAt;

    private boolean draft;

    @Convert(converter = SummaryConverter.class)
    private Summary summary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "username", referencedColumnName = "username")
    private User user;

    @OneToMany(mappedBy = "form", cascade = {CascadeType.ALL}, orphanRemoval = true)
    private List<FeedbackFormQuestion> questions = new ArrayList<>();

    @OneToMany(mappedBy = "form", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<FeedbackFormAnswer> answers = new ArrayList<>();

    @OneToMany(mappedBy = "form", cascade = {CascadeType.ALL}, orphanRemoval = true)
    private List<Recipient> recipients = new ArrayList<>();

    public void updateQuestions(Collection<FeedbackFormQuestion> questions) {
        // Remove questions that are no longer in the new collection
        var newQuestionKeys = questions.stream()
                .map(FeedbackFormQuestion::getQuestionKey)
                .collect(Collectors.toSet());
        this.questions.removeIf(q -> !newQuestionKeys.contains(q.getQuestionKey()));

        // Update or add questions
        for (FeedbackFormQuestion newQuestion : questions) {
            var existing = this.questions.stream()
                    .filter(q -> newQuestion.getQuestionKey().equals(q.getQuestionKey()))
                    .findFirst();
            if (existing.isPresent()) {
                existing.get().setType(newQuestion.getType());
                existing.get().setRequired(newQuestion.isRequired());
            } else {
                this.questions.add(newQuestion);
            }
        }
    }

    public void updateRecipients(Collection<Recipient> recipients) {
        // Remove recipients that are no longer in the new collection
        var newRecipientIds = recipients.stream()
                .map(Recipient::getRecipient)
                .collect(Collectors.toSet());
        this.recipients.removeIf(r -> !newRecipientIds.contains(r.getRecipient()));

        // Add new recipients
        for (Recipient newRecipient : recipients) {
            var exists = this.recipients.stream()
                    .anyMatch(r -> newRecipient.getRecipient().equals(r.getRecipient()));
            if (!exists) {
                this.recipients.add(newRecipient);
            }
        }
    }


}
