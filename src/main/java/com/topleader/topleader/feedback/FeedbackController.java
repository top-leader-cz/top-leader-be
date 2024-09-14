package com.topleader.topleader.feedback;


import com.topleader.topleader.feedback.api.*;

import com.topleader.topleader.feedback.entity.FeedbackForm;
import com.topleader.topleader.feedback.entity.FeedbackFormQuestion;
import com.topleader.topleader.feedback.entity.Question;
import com.topleader.topleader.feedback.entity.Recipient;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/latest/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;


    @Transactional
    @GetMapping("/{id}")
    @Secured({"ADMIN", "HR", "COACH", "USER"})
    public FeedbackFormDto getForm(@PathVariable long id) {
        return FeedbackFormDto.witAnswer(feedbackService.fetchForm(id));
    }

    @Transactional
    @GetMapping("/user/{username}")
    @Secured({"ADMIN", "HR", "COACH", "USER"})
    public List<FeedbackForms> getForms(@PathVariable String username) {
       return FeedbackForms.of(feedbackService.fetchForms(username));
    }

    @PostMapping
    @Secured({"ADMIN", "HR", "COACH", "USER"})
    @Transactional
    public FeedbackFormDto createForm(@RequestBody @Valid FeedbackFormRequest request) {
       var form = saveForm(request);
        if(!request.isDraft()) {
            feedbackService.sendFeedbacks(getFeedbackData(request, form));
        }
       return feedbackService.toFeedbackFormDto(form);
    }

    private FeedbackForm saveForm(FeedbackFormRequest request) {
        var defaultKeys = feedbackService.fetchOptions().stream().map(Question::getKey)
                .collect(Collectors.toList());
        feedbackService.updateQuestions(toQuestions(request.getQuestions(), defaultKeys));
        var form = feedbackService.saveForm(FeedbackFormRequest.toSimpleForm(request));
        return feedbackService.saveForm(FeedbackFormRequest.toForm(request.setId(form.getId())));
    }

    private List<Question> toQuestions(List<QuestionDto> questions, List<String> defaultKeys) {
        return questions
                .stream()
                .map(q -> new Question().setKey(q.key()).setDefaultQuestion(defaultKeys.contains(q.key())))
                .collect(Collectors.toList());
    }

    @PutMapping("/{id}")
    @Secured({"ADMIN", "HR", "COACH", "USER"})
    @Transactional
    public FeedbackFormDto updateForm(@PathVariable long id,  @RequestBody @Valid FeedbackFormRequest request) {
        var savedForm = feedbackService.fetchForm(id);
        savedForm.setTitle(request.getTitle());
        savedForm.setDescription(request.getDescription());
        savedForm.setQuestions(FeedbackFormRequest.toQuestions(request.getQuestions(), savedForm));
        savedForm.getRecipients().addAll(FeedbackFormRequest.toRecipients(request.getRecipients(), savedForm));
        var form = feedbackService.saveForm(savedForm);
        if(!request.isDraft()) {
            feedbackService.sendFeedbacks(getFeedbackData(request, form));
        }
        return feedbackService.toFeedbackFormDto(form);
    }

    @DeleteMapping("/{id}")
    @Secured({"ADMIN", "HR", "COACH", "USER"})
    public void deleteForm(@PathVariable long id) {
        feedbackService.deleteForm(id);
    }


    public FeedbackData getFeedbackData(FeedbackFormRequest request, FeedbackForm form) {
        var byUsername = form.getRecipients().stream()
                .collect(Collectors.toMap(Recipient::getRecipient, Function.identity()));
        return new FeedbackData().setLocale(request.getLocale())
                .setValidTo(request.getValidTo())
                .setFormId(form.getId())
                .setFirstName(form.getUser().getFirstName())
                .setLastName(form.getUser().getLastName())
                .setRecipients(form.getRecipients().stream()
                        .map(r -> new FeedbackData.Recipient(byUsername.get(r.getRecipient()).getId(), r.getRecipient(), r.getToken(), r.isSubmitted()) )
                        .collect(Collectors.toList()));
    }

}
