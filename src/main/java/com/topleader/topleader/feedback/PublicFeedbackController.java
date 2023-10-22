package com.topleader.topleader.feedback;

import com.topleader.topleader.feedback.api.FeedbackFormDto;
import com.topleader.topleader.feedback.api.FeedbackSubmitRequest;
import com.topleader.topleader.feedback.entity.Recipient;
import com.topleader.topleader.feedback.exception.InvalidFormOrRecipientException;
import com.topleader.topleader.feedback.repository.FeedbackFormAnswerRepository;
import com.topleader.topleader.user.User;
import com.topleader.topleader.user.UserDetailService;
import com.topleader.topleader.util.common.user.UserUtils;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/latest/feedback")
@RequiredArgsConstructor
public class PublicFeedbackController {

    private final FeedbackService feedbackService;

    private final UserDetailService userDetailService;


    @Transactional
    @GetMapping("/{formId}/{username}/{token}")
    public FeedbackFormDto getForm(@PathVariable long formId, @PathVariable String username, @PathVariable String token) {
        feedbackService.getRecipientIfValid(formId, username, token);
        userDetailService.getUser(username)
                .ifPresent(u -> userDetailService.save(u.setStatus(User.Status.VIEWED)));
        return FeedbackFormDto.of(feedbackService.fetchForm(formId));
    }

    @PostMapping("/{formId}/{username}/{token}")
    public void submitForm(@PathVariable long formId, @PathVariable String username, @PathVariable String token,
                                      @RequestBody @Valid FeedbackSubmitRequest request) {
        var recipient = feedbackService.getRecipientIfValid(formId, username, token);
        feedbackService.submitForm(FeedbackSubmitRequest.toAnswers(request, formId, recipient), username);
    }

}
