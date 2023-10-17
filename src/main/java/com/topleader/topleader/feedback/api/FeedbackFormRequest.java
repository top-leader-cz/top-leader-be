package com.topleader.topleader.feedback.api;

import com.topleader.topleader.feedback.FeedbackController;
import com.topleader.topleader.feedback.entity.FeedbackForm;
import com.topleader.topleader.feedback.entity.FeedbackFormQuestion;
import com.topleader.topleader.feedback.entity.Question;
import com.topleader.topleader.feedback.entity.Recipient;
import com.topleader.topleader.user.User;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Accessors(chain = true)
public class FeedbackFormRequest {

    private Long id;

    @NotNull
    private String title;

    private String description;

    @NotNull
    private String username;

    @NotNull
    private LocalDateTime validTo;

    @NotNull
    private Set<QuestionDto> questions;

    @NotNull
    private Set<RecipientDto> recipients;

    public static FeedbackForm toForm(FeedbackFormRequest request) {
        var feedbackForm = new FeedbackForm()
                .setId(request.getId())
                .setTitle(request.getTitle())
                .setDescription(request.getDescription())
                .setValidTo(request.getValidTo())
                .setUser(new User().setUsername(request.getUsername()));

        var feedbackFormQuestion = request.getQuestions().stream()
                .map(q -> {
                    var question = new Question().setKey(q.key());
                    return new FeedbackFormQuestion()
                            .setId(new FeedbackFormQuestion.FeedbackFormQuestionId(request.getId(), q.key()))
                            .setType(q.type())
                            .setRequired(q.required())
                            .setQuestion(question)
                            .setForm(feedbackForm);
                })
                .collect(Collectors.toList());
        feedbackForm.setQuestions(feedbackFormQuestion);

        var recipients = request.getRecipients().stream()
                .map(r -> new Recipient().setId(r.id())
                        .setRecipient(r.username())
                        .setSubmitted(r.submitted())
                        .setForm(feedbackForm))
                .collect(Collectors.toList());
        feedbackForm.setRecipients(recipients);

        return feedbackForm;
    }
}