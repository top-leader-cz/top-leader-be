package com.topleader.topleader.feedback.api;

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

    private LocalDateTime validTo;

    private List<QuestionDto> questions;

    private List<RecipientDto> recipients;

    public static FeedbackFormDto of(FeedbackForm feedbackForm) {
        var questions = feedbackForm.getQuestions().stream()
                .map(q -> {
                    var question = q.getQuestion();
                    return new QuestionDto(question.getKey(), q.getType(), q.isRequired(), List.of());
                })
                .collect(Collectors.toList());

        var recipients = feedbackForm.getRecipients().stream()
                .map(r -> new RecipientDto(r.getId(), r.getRecipient(), r.isSubmitted()))
                .collect(Collectors.toList());

        return new FeedbackFormDto()
                .setTitle(feedbackForm.getTitle())
                .setUsername(feedbackForm.getUser().getUsername())
                .setDescription(feedbackForm.getDescription())
                .setId(feedbackForm.getId())
                .setValidTo(feedbackForm.getValidTo())
                .setQuestions(questions)
                .setRecipients(recipients);
    }

    public static FeedbackFormDto witAnswer(FeedbackForm feedbackForm) {
       var dto =  of(feedbackForm);

        var result = new HashMap<String, List<AnswerRecipientDto>>();
        feedbackForm.getAnswers().forEach(a -> {
                    result.compute(a.getQuestion().getKey(), (v, k) -> {
                        var answers = result.getOrDefault(a.getQuestion().getKey(), new ArrayList<>());
                        answers.add(new AnswerRecipientDto(a.getAnswer(), a.getRecipient().getRecipient()));
                        return answers;
                    });
                });

            dto.setQuestions(dto.getQuestions().stream()
                    .map(q -> new QuestionDto(q.key(), q.type(), q.required(), result.getOrDefault(q.key(), List.of())))
                    .collect(Collectors.toList()));


            return dto;
        }
}

