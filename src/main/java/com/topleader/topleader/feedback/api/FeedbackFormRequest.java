package com.topleader.topleader.feedback.api;

import com.topleader.topleader.feedback.entity.FeedbackForm;
import com.topleader.topleader.feedback.entity.FeedbackFormQuestion;
import com.topleader.topleader.feedback.entity.Question;
import com.topleader.topleader.feedback.entity.Recipient;
import com.topleader.topleader.common.util.common.CommonUtils;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
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
    private List<QuestionDto> questions;

    @NotNull
    private List<RecipientDto> recipients;

    @NotEmpty
    private String locale;

    private boolean draft;

    public static FeedbackForm toForm(FeedbackFormRequest request) {
        return new FeedbackForm()
                .setTitle(request.getTitle())
                .setDescription(request.getDescription())
                .setValidTo(request.getValidTo())
                .setCreatedAt(LocalDateTime.now())
                .setUsername(request.getUsername().toLowerCase(Locale.ROOT))
                .setDraft(request.isDraft());
    }

    public static FeedbackForm toSimpleForm(FeedbackFormRequest request) {
        return new FeedbackForm()
                .setId(request.getId())
                .setTitle(request.getTitle())
                .setDescription(request.getDescription())
                .setValidTo(request.getValidTo())
                .setCreatedAt(LocalDateTime.now())
                .setUsername(request.getUsername().toLowerCase(Locale.ROOT))
                .setDraft(request.isDraft());
    }

    public static List<FeedbackFormQuestion> toQuestions(List<QuestionDto> questions, long feedbackFormId) {
        return questions.stream()
                .map(q -> new FeedbackFormQuestion()
                        .setFeedbackFormId(feedbackFormId)
                        .setQuestionKey(q.key())
                        .setType(q.type())
                        .setRequired(q.required()))
                .collect(Collectors.toList());
    }

    public static List<Recipient> toRecipients(List<RecipientDto> recipients, long feedbackFormId) {
        return recipients.stream()
                .map(r -> new Recipient()
                        .setId(r.id())
                        .setFormId(feedbackFormId)
                        .setToken(r.id() != null ? null : CommonUtils.generateToken())
                        .setRecipient(r.username())
                        .setSubmitted(r.submitted()))
                .collect(Collectors.toList());
    }
}