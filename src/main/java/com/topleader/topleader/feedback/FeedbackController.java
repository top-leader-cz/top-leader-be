package com.topleader.topleader.feedback;


import com.topleader.topleader.email.EmailService;
import com.topleader.topleader.feedback.api.*;

import com.topleader.topleader.feedback.entity.FeedbackForm;
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
    @GetMapping("/options")
    @Secured({"ADMIN", "HR", "COACH", "USER"})
    public FeedbackFormOptions getOptions() {
        return FeedbackFormOptions.of(feedbackService.fetchQuestions());
    }

    @Transactional
    @GetMapping("/{id}")
    @Secured({"ADMIN", "HR", "COACH", "USER"})
    public FeedbackFormDto getForm(@PathVariable long id) {
        return FeedbackFormDto.of(feedbackService.fetchForm(id));
    }

    @Transactional
    @GetMapping("/user/{username}")
    @Secured({"ADMIN", "HR", "COACH", "USER"})
    public List<FeedbackForms> getForms(@PathVariable String username) {
       return FeedbackForms.of(feedbackService.fetchForms(username));
    }

    @PostMapping
    @Secured({"ADMIN", "HR", "COACH", "USER"})
    public FeedbackFormDto createForm(@RequestBody @Valid FeedbackFormRequest request) {
       var form = feedbackService.saveForm(FeedbackFormRequest.toForm(request));
       feedbackService.sendFeedbacks(getFeedbackData(request, form));
       return feedbackService.toFeedbackFormDto(form);
    }


    @PutMapping("/{id}")
    @Secured({"ADMIN", "HR", "COACH", "USER"})
    public FeedbackFormDto updateForm(@PathVariable long id,  @RequestBody @Valid FeedbackFormRequest request) {
        var form = feedbackService.saveForm(FeedbackFormRequest.toForm(request).setId(id));
        feedbackService.sendFeedbacks(getFeedbackData(request, form));
        return feedbackService.toFeedbackFormDto(form);
    }

    @DeleteMapping("/{id}")
    @Secured({"ADMIN", "HR", "COACH", "USER"})
    public void deleteForm(@PathVariable long id) {
        feedbackService.deleteForm(id);
    }


    public FeedbackData getFeedbackData(FeedbackFormRequest request, FeedbackForm form) {
        var byUsername = request.getRecipients().stream()
                .collect(Collectors.toMap(RecipientDto::username, Function.identity()));
        return new FeedbackData().setLocale(request.getLocale())
                .setValidTo(request.getValidTo())
                .setFormId(form.getId())
                .setRecipients(form.getRecipients().stream()
                        .map(r -> new FeedbackData.Recipient(byUsername.get(r.getRecipient()).id(), r.getRecipient(), r.getToken()))
                        .collect(Collectors.toList()));
    }

}
