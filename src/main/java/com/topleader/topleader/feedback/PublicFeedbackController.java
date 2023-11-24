package com.topleader.topleader.feedback;

import com.topleader.topleader.feedback.api.FeedbackFormDto;
import com.topleader.topleader.feedback.api.FeedbackFormOptions;
import com.topleader.topleader.feedback.api.FeedbackSubmitRequest;
import com.topleader.topleader.feedback.api.NewUser;
import com.topleader.topleader.user.User;
import com.topleader.topleader.user.UserDetailService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/latest/feedback")
@RequiredArgsConstructor
public class PublicFeedbackController {

    private final FeedbackService feedbackService;

    private final UserDetailService userDetailService;

    @Transactional
    @GetMapping("/options")
    public FeedbackFormOptions getOptions() {
        return FeedbackFormOptions.of(feedbackService.fetchQuestions());
    }


    @Transactional
    @GetMapping("/{formId}/{username}/{token}")
    public FeedbackFormDto getForm(@PathVariable long formId, @PathVariable String username, @PathVariable String token) {
        feedbackService.validateRecipientIfValid(formId, username, token);
        userDetailService.getUser(username)
                .ifPresent(u -> userDetailService.save(u.setStatus(User.Status.VIEWED)));
        return FeedbackFormDto.of(feedbackService.fetchForm(formId));
    }

    @PostMapping("/{formId}/{username}/{token}")
    public void submitForm(@PathVariable long formId, @PathVariable String username, @PathVariable String token,
                                      @RequestBody @Valid FeedbackSubmitRequest request) {
        var recipient = feedbackService.validateRecipientIfValid(formId, username, token);
        feedbackService.submitForm(FeedbackSubmitRequest.toAnswers(request, formId, recipient), username);
    }


    @PostMapping("/request-access/{formId}/{username}/{token}")
    public void newUser(@PathVariable long formId, @PathVariable String username, @PathVariable String token,
                                   @RequestBody @Valid NewUser newUser) {
        feedbackService.validateRecipientIfSubmitted(formId, username, token);
        userDetailService.getUser(username)
                .ifPresent(u ->{
                    userDetailService.save(u.setStatus(User.Status.PENDING)
                            .setUsername(newUser.getEmail())
                            .setFirstName(newUser.getFirstName())
                            .setLastName(newUser.getLastName())
                            .setCompany(newUser.getCompany())
                            .setHrEmail(newUser.getHrEmail()));
                    if(!username.equals(newUser.getEmail()));{
                        userDetailService.delete(username);
                    }
                });

    }

}
