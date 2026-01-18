package com.topleader.topleader.feedback.api;

import com.topleader.topleader.feedback.entity.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Accessors(chain = true)
public class FeedbackSubmitRequest {

    @NotNull
    private List<AnswerDto> answers;

    public static List<FeedbackFormAnswer> toAnswers(FeedbackSubmitRequest request, long formId, Recipient recipient) {
        recipient.setSubmitted(true);
        return request.getAnswers().stream()
                .map(answerDto -> new FeedbackFormAnswer()
                                .setFormId(formId)
                                .setRecipientId(recipient.getId())
                                .setQuestionKey(answerDto.question())
                                .setAnswer(answerDto.answer()))
                .toList();
    }

    public record AnswerDto(String question, String answer) {
    }
}
