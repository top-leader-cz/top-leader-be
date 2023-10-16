package com.topleader.topleader.feedback.api;

import com.topleader.topleader.feedback.entity.Recipient;
import lombok.Data;
import lombok.experimental.Accessors;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Accessors(chain = true)
public class FeedbackFormDto {
    private Long id;

    private String title;

    private String description;

    private LocalDateTime validTo;

    private List<QuestionDto> questions;

    private List<String> recipients;

    public static FeedbackFormDto of(com.topleader.topleader.feedback.entity.FeedbackForm feedbackForm) {
        var questions = feedbackForm.getQuestions().stream()
                .map(q -> {
                    var question = q.getQuestion();
                    return new QuestionDto(question.getKey(), question.getType(), q.isRequired());
                })
                .collect(Collectors.toList());

        var recipients = feedbackForm.getRecipients().stream()
                .map(Recipient::getRecipient)
                .collect(Collectors.toList());

        return new FeedbackFormDto()
                .setTitle(feedbackForm.getTitle())
                .setDescription(feedbackForm.getDescription())
                .setId(feedbackForm.getId())
                .setValidTo(feedbackForm.getValidTo())
                .setQuestions(questions)
                .setRecipients(recipients);
    }
}

