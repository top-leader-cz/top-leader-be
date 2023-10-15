package com.topleader.topleader.feedback;


import com.topleader.topleader.feedback.entity.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController("/api/latest/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    private static final String SCALE_KEY = "scale.";

    @GetMapping("/options")
    public FeedbackFormOptions getOptions() {
        return FeedbackFormOptions.of(feedbackService.fetchQuestions());
    }
    @GetMapping("/{id}")
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
    public static class FeedbackFormDto {
        private Long id;

        private String title;

        private String description;

        private String link;

        private LocalDateTime validTo;

        private Set<QuestionDto> questions;

        private Set<String> recipients;

        public static FeedbackFormDto of(FeedbackForm feedbackForm) {
             var questions = feedbackForm.getQuestions().stream()
                    .map(q -> {
                        var question = q.getQuestion();
                        return new QuestionDto(question.getKey(), question.getType(), q.isRequired());
                    })
                     .collect(Collectors.toSet());
            var recipients = feedbackForm.getRecipients().stream()
                    .map(Recipient::getRecipient)
                    .collect(Collectors.toSet());

            return new FeedbackFormDto()
                    .setId(feedbackForm.getId())
                    .setLink(feedbackForm.getLink())
                    .setValidTo(feedbackForm.getValidTo())
                    .setQuestions(questions)
                    .setRecipients(recipients);
        }
     }

    @Data
    @Accessors(chain = true)
    public static class FeedbackFormOptions {

        private Map<QuestionDto, List<String>> items;


        private Set<String> scales;

        public static FeedbackFormOptions of(List<Question> questions) {
            var items = new HashMap<QuestionDto, List<String>>();
            questions.forEach(question -> {
                        var questionDto = new QuestionDto(question.getKey(), question.getType(),);
                        items.compute(questionDto, (key, value) -> {
                            var answers = items.getOrDefault(questionDto, new ArrayList<>());
                            answers.addAll(question.getAnswers().stream()
                                    .map(Answer::getKey)
                                    .collect(Collectors.toSet()));
                            return answers;
                        });
                    });

            var scales = IntStream.range(1, 11)
                    .mapToObj(i -> SCALE_KEY + i)
                    .collect(Collectors.toSet());

            return new FeedbackFormOptions()
                    .setItems(items)
                    .setScales(scales);
        }
    }

    public record QuestionDto(String key, Question.Type type, boolean required) {
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
                    .setLink(request.getLink())
                    .setValidTo(request.getValidTo());

            var feedbackFormQuestion = request.getQuestions().stream()
                    .map(q -> {
                        var question = new Question().setType(q.type).setKey(q.key);
                        return new FeedbackFormQuestion().setRequired(q.required)
                                .setQuestion(question)
                                .setFeedbackForm(feedbackForm);
                    })
                            .collect(Collectors.toSet());

            feedbackForm.setQuestions(feedbackFormQuestion);

            var recipients = request.getRecipients().stream()
                    .map(r -> new Recipient().setRecipient(r).setFeedbackForm(feedbackForm))
                    .collect(Collectors.toSet());
            feedbackForm.setRecipients(recipients);

            return feedbackForm;
        }
    }

}
