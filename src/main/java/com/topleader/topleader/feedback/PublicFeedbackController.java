package com.topleader.topleader.feedback;

import com.topleader.topleader.email.EmailService;
import com.topleader.topleader.feedback.api.FeedbackFormDto;
import com.topleader.topleader.feedback.api.FeedbackFormOptions;
import com.topleader.topleader.feedback.api.FeedbackSubmitRequest;
import com.topleader.topleader.feedback.api.NewUser;
import com.topleader.topleader.user.User;
import com.topleader.topleader.user.UserDetailService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.Locale;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import static com.topleader.topleader.user.User.Status.*;

@Slf4j
@RestController
@RequestMapping("/api/public/latest/feedback")
@RequiredArgsConstructor
public class PublicFeedbackController {

    private final FeedbackService feedbackService;

    private final UserDetailService userDetailService;

    private final EmailService emailService;

    @Transactional
    @GetMapping("/options")
    public FeedbackFormOptions getOptions() {
        return FeedbackFormOptions.of(feedbackService.fetchOptions());
    }


    @Transactional
    @GetMapping("/{formId}/{username}/{token}")
    public FeedbackFormDto getForm(@PathVariable long formId, @PathVariable String username, @PathVariable String token) {
        feedbackService.validateRecipientIfValid(formId, username, token);
        userDetailService.getUser(username)
                .ifPresent(u -> {
                    if (skipUpdate(u)) return;
                    userDetailService.save(u.setStatus(User.Status.VIEWED));
                });
        return FeedbackFormDto.of(feedbackService.fetchForm(formId));
    }

    @PostMapping("/{formId}/{username}/{token}")
    public void submitForm(@PathVariable long formId, @PathVariable String username, @PathVariable String token,
                           @RequestBody @Valid FeedbackSubmitRequest request) {
        log.info("submition answers for respondent: [{}] ", username);
        var recipient = feedbackService.validateRecipientIfValid(formId, username, token);
        feedbackService.submitForm(FeedbackSubmitRequest.toAnswers(request, formId, recipient), username);
        feedbackService.generateSummary(formId);
    }


    @PostMapping("/request-access/{formId}/{username}/{token}")
    public void newUser(@PathVariable long formId, @PathVariable String username, @PathVariable String token,
                        @RequestBody @Valid NewUser newUser) {
        log.info("Receiving respondent form. Respondent: [{}] ", username);
        feedbackService.validateRecipientIfSubmitted(formId, username, token);
        userDetailService.getUser(username)
                .ifPresentOrElse(u -> {
                    if (skipUpdate(u)) return;
                    log.info("Updating respondent: [{}] ", username);
                    userDetailService.save(u.setStatus(User.Status.PENDING)
                            .setUsername(newUser.getEmail())
                            .setFirstName(newUser.getFirstName())
                            .setLastName(newUser.getLastName())
                            .setHrEmail(newUser.getHrEmail()));
                }, () -> newUser(newUser, username));

        var body = String.format("Username: %s Timestamp: %s", username,  LocalDateTime.now());
        emailService.sendEmail("info@topleader.io", "New Pending user in the TopLeader platform", body);
    }

    boolean skipUpdate(User u) {
        return AUTHORIZED == u.getStatus() || PAID == u.getStatus() || PENDING == u.getStatus();
    }


    private void newUser(NewUser newUser, String username) {
        log.info("Creating respondent: [{}] ", username);
        userDetailService.save(new User().setStatus(User.Status.PENDING)
                .setUsername(newUser.getEmail().toLowerCase(Locale.ROOT))
                .setUsername(username)
                .setFirstName(newUser.getFirstName())
                .setLastName(newUser.getLastName())
                .setHrEmail(newUser.getHrEmail()));
    }

}
