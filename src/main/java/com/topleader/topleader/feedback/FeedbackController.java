package com.topleader.topleader.feedback;


import com.topleader.topleader.email.EmailService;
import com.topleader.topleader.feedback.api.FeedbackFormDto;
import com.topleader.topleader.feedback.api.FeedbackFormOptions;
import com.topleader.topleader.feedback.api.FeedbackFormRequest;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/latest/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    private EmailService emailService;

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
    @PostMapping
    @Secured({"ADMIN", "HR", "COACH", "USER"})
    public FeedbackFormDto createForm(@RequestBody @Valid FeedbackFormRequest request) {
        var form = feedbackService.saveForm(FeedbackFormRequest.toForm(request));
//        form.getRecipients().forEach(r -> emailService.sendEmail(r.getRecipient(), "em"));
        return FeedbackFormDto.of(form);
    }

    @Transactional
    @PutMapping("/{id}")
    @Secured({"ADMIN", "HR", "COACH", "USER"})
    public FeedbackFormDto updateForm(@PathVariable long id,  @RequestBody @Valid FeedbackFormRequest request) {
        var form = FeedbackFormRequest.toForm(request).setId(id);
        return FeedbackFormDto.of(feedbackService.saveForm(form));
    }

    @DeleteMapping("/{id}")
    @Secured({"ADMIN", "HR", "COACH", "USER"})
    public void deleteForm(@PathVariable long id) {
        feedbackService.deleteForm(id);
    }

}
