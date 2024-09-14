package com.topleader.topleader.feedback.api;

import com.topleader.topleader.feedback.entity.FeedbackForm;
import com.topleader.topleader.feedback.entity.FeedbackFormQuestion;
import com.topleader.topleader.feedback.entity.Question;
import com.topleader.topleader.feedback.entity.Recipient;
import com.topleader.topleader.user.User;
import com.topleader.topleader.util.common.CommonUtils;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class FeedbackFormRequest {

    private Long id;

    @NotNull
    private String title;

    private String description;

    @NotNull
    @Email
    private String username;

    private LocalDateTime validTo;

    @NotNull
    private List<QuestionDto> questions = List.of();

    @NotNull
    private List<RecipientDto> recipients = List.of();

    @NotEmpty
    private String locale;

    private boolean draft;

    public static FeedbackForm toForm(FeedbackFormRequest request) {
        var feedbackForm = new FeedbackForm()
                .setId(request.getId())
                .setTitle(request.getTitle())
                .setDescription(request.getDescription())
                .setValidTo(request.getValidTo())
                .setCreatedAt(LocalDateTime.now())
                .setUser(new User().setUsername(request.getUsername().toLowerCase(Locale.ROOT)))
                .setDraft(request.isDraft());

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
                .collect(Collectors.toSet());
        feedbackForm.setQuestions(feedbackFormQuestion);

        var recipients = request.getRecipients().stream()
                .map(r -> new Recipient().setId(r.id())
                        .setForm(feedbackForm)
                        .setToken(CommonUtils.generateToken())
                        .setRecipient(r.username())
                        .setSubmitted(r.submitted())
                        .setForm(feedbackForm))
                .collect(Collectors.toSet());
        feedbackForm.setRecipients(recipients);

        return feedbackForm;
    }

    public static FeedbackForm toSimpleForm(FeedbackFormRequest request) {
        return new FeedbackForm()
                .setId(request.getId())
                .setTitle(request.getTitle())
                .setDescription(request.getDescription())
                .setValidTo(request.getValidTo())
                .setCreatedAt(LocalDateTime.now())
                .setUser(new User().setUsername(request.getUsername().toLowerCase(Locale.ROOT)))
                .setDraft(request.isDraft());
    }

    public static Set<FeedbackFormQuestion> toQuestions(List<QuestionDto> questions, FeedbackForm feedbackForm) {
        return questions.stream()
                .map(q -> {
                    var question = new Question().setKey(q.key());
                    return new FeedbackFormQuestion()
                            .setId(new FeedbackFormQuestion.FeedbackFormQuestionId(feedbackForm.getId(), q.key()))
                            .setType(q.type())
                            .setRequired(q.required())
                            .setQuestion(question)
                            .setForm(feedbackForm);
                })
                .collect(Collectors.toSet());
    }

    public static Set<Recipient> toRecipients(List<RecipientDto> recipients, FeedbackForm feedbackForm) {
        return recipients.stream()
                .map(r -> new Recipient().setId(r.id())
                        .setToken(CommonUtils.generateToken())
                        .setRecipient(r.username())
                        .setSubmitted(r.submitted())
                        .setForm(feedbackForm))
                .collect(Collectors.toSet());
    }

}