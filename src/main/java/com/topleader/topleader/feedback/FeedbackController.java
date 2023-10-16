package com.topleader.topleader.feedback;


import com.topleader.topleader.feedback.api.FeedbackFormDto;
import com.topleader.topleader.feedback.api.FeedbackFormOptions;
import com.topleader.topleader.feedback.entity.*;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/latest/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;


    @Transactional
    @GetMapping("/options")
    @Secured({"ADMIN", "HR", "COACH", "USER"})
    public FeedbackFormOptions getOptions() {
        return FeedbackFormOptions.of(feedbackService.fetchQuestions());
    }

    @Transactional
    @GetMapping("/{id}")
    @Secured({"ADMIN", "HR", "COACH", "USER"})
    public FeedbackFormDto getForm(@PathVariable long id) {
        return FeedbackFormDto.of(feedbackService.fetch(id));
    }

    @PostMapping()
    public FeedbackFormDto createForm(@RequestBody @Valid FeedbackFormRequest request) {
        var form = feedbackService.create(FeedbackFormRequest.toForm(request));
        return FeedbackFormDto.of(form);
    }


    @Data
    @Accessors(chain = true)
    public static class FeedbackFormRequest {

        @NotNull
        private String title;

        private String description;

        @NotNull
        private String link;

        @NotNull
        private LocalDateTime validTo;

        private Set<QuestionDto> questions;

        private Set<String> recipients;

        public static FeedbackForm toForm(FeedbackFormRequest request) {

            var feedbackForm = new FeedbackForm().setTitle(request.getTitle())
                    .setDescription(request.getDescription())
                    .setValidTo(request.getValidTo());

            var feedbackFormQuestion = request.getQuestions().stream()
                    .map(q -> {
                        var question = new Question().setType(q.type).setKey(q.key);
                        return new FeedbackFormQuestion().setRequired(q.required)
                                .setQuestion(question)
                                .setFeedbackForm(feedbackForm);
                    })
                            .collect(Collectors.toList());

            feedbackForm.setQuestions(feedbackFormQuestion);

            var recipients = request.getRecipients().stream()
                    .map(r -> new Recipient().setRecipient(r).setForm(feedbackForm))
                    .collect(Collectors.toList());
            feedbackForm.setRecipients(recipients);

            return feedbackForm;
        }
    }
    public record QuestionDto(String key, Question.Type type, boolean required) {
    }


}
