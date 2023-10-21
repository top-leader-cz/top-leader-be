package com.topleader.topleader.feedback;

import com.topleader.topleader.feedback.api.FeedbackFormDto;
import com.topleader.topleader.feedback.api.FeedbackSubmitRequest;
import com.topleader.topleader.feedback.entity.Recipient;
import com.topleader.topleader.feedback.exception.InvalidFormOrRecipientException;
import com.topleader.topleader.feedback.repository.FeedbackFormAnswerRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/latest/feedback")
@RequiredArgsConstructor
public class PublicFeedbackController {

    private final FeedbackService feedbackService;


    @Transactional
    @GetMapping("/{formId}/{username}/{token}")
    public FeedbackFormDto getForm(@PathVariable long formId, @PathVariable String username, @PathVariable String token) {
        feedbackService.getRecipientIfValid(formId, username, token);
        return FeedbackFormDto.of(feedbackService.fetchForm(formId));
    }

    @PostMapping("/{formId}/{username}/{token}")
    public void submitForm(@PathVariable long formId, @PathVariable String username, @PathVariable String token,
                                      @RequestBody @Valid FeedbackSubmitRequest request) {
        var recipient = feedbackService.getRecipientIfValid(formId, username, token);
        feedbackService.submitForm(FeedbackSubmitRequest.toAnswers(request, formId, recipient));
    }

}
