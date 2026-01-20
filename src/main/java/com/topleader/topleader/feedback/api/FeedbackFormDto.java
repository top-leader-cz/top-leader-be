package com.topleader.topleader.feedback.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.topleader.topleader.feedback.entity.FeedbackForm;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Accessors(chain = true)
public class FeedbackFormDto {


    private Long id;

    private String title;

    private String description;

    private String username;

    private String firstName;

    private String lastName;

    private LocalDateTime validTo;

    private List<QuestionDto> questions;

    private List<RecipientDto> recipients;

    private Summary summary;

    private boolean draft;

    public static FeedbackFormDto of(FeedbackForm feedbackForm, List<com.topleader.topleader.feedback.entity.FeedbackFormQuestion> formQuestions, List<com.topleader.topleader.feedback.entity.Recipient> formRecipients) {
        var questions = formQuestions.stream()
                .map(q -> new QuestionDto(q.getQuestionKey(), q.getType(), q.isRequired(), List.of()))
                .collect(Collectors.toList());

        var recipients = formRecipients.stream()
                .map(r -> new RecipientDto(r.getId(), r.getRecipient(), r.isSubmitted()))
                .collect(Collectors.toList());

        return new FeedbackFormDto()
                .setTitle(feedbackForm.getTitle())
                .setUsername(feedbackForm.getUsername())
                .setDescription(feedbackForm.getDescription())
                .setId(feedbackForm.getId())
                .setValidTo(feedbackForm.getValidTo())
                .setQuestions(questions)
                .setRecipients(recipients)
                .setSummary(feedbackForm.getSummary())
                .setDraft(feedbackForm.isDraft());
    }

    public static FeedbackFormDto of(FeedbackForm feedbackForm, List<com.topleader.topleader.feedback.entity.FeedbackFormQuestion> formQuestions, List<com.topleader.topleader.feedback.entity.Recipient> formRecipients, String firstName, String lastName) {
        return of(feedbackForm, formQuestions, formRecipients)
                .setFirstName(firstName)
                .setLastName(lastName);
    }

    public static FeedbackFormDto witAnswer(FeedbackForm feedbackForm, List<com.topleader.topleader.feedback.entity.FeedbackFormQuestion> formQuestions, List<com.topleader.topleader.feedback.entity.Recipient> formRecipients, List<com.topleader.topleader.feedback.entity.FeedbackFormAnswer> formAnswers, java.util.Map<Long, com.topleader.topleader.feedback.entity.Recipient> recipientMap) {
        var dto = of(feedbackForm, formQuestions, formRecipients);

        var result = new HashMap<String, List<AnswerRecipientDto>>();
        formAnswers.forEach(a -> {
            var recipient = recipientMap.get(a.getRecipientId());
            if (recipient != null) {
                result.compute(a.getQuestionKey(), (v, k) -> {
                    var answers = result.getOrDefault(a.getQuestionKey(), new ArrayList<>());
                    answers.add(new AnswerRecipientDto(a.getAnswer(), recipient.getRecipient()));
                    return answers;
                });
            }
        });

        dto.setQuestions(dto.getQuestions().stream()
                .map(q -> new QuestionDto(q.key(), q.type(), q.required(), result.getOrDefault(q.key(), List.of())))
                .collect(Collectors.toList()));

        dto.setSummary(feedbackForm.getSummary());
        return dto;
    }

    @JsonIgnore
    public long getAnswersCount() {
        return recipients.stream()
                .filter(RecipientDto::submitted)
                .count();
    }

    @JsonIgnore
    public boolean allowSummary(int summaryLimit) {
        return getAnswersCount() >= summaryLimit;
    }
}

