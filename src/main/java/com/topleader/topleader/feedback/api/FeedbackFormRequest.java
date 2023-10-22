package com.topleader.topleader.feedback.api;

import com.topleader.topleader.feedback.FeedbackController;
import com.topleader.topleader.feedback.entity.FeedbackForm;
import com.topleader.topleader.feedback.entity.FeedbackFormQuestion;
import com.topleader.topleader.feedback.entity.Question;
import com.topleader.topleader.feedback.entity.Recipient;
import com.topleader.topleader.user.User;
import com.topleader.topleader.util.common.CommonUtils;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;
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
    private List<QuestionDto> questions;

    @NotNull
    private List<RecipientDto> recipients;

    @Pattern(regexp = "[a-z]{2}")
    private String locale;

    public static FeedbackForm toForm(FeedbackFormRequest request) {
        var feedbackForm = new FeedbackForm()
                .setId(request.getId())
                .setTitle(request.getTitle())
                .setDescription(request.getDescription())
                .setValidTo(request.getValidTo())
                .setCreatedAt(LocalDateTime.now())
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
                        .setForm(feedbackForm)
                        .setToken(CommonUtils.generateToken())
                        .setRecipient(r.username())
                        .setSubmitted(r.submitted())
                        .setForm(feedbackForm))
                .collect(Collectors.toList());
        feedbackForm.setRecipients(recipients);

        return feedbackForm;
    }

}