package com.topleader.topleader.feedback;


import com.topleader.topleader.common.exception.ApiValidationException;
import com.topleader.topleader.feedback.api.*;

import com.topleader.topleader.feedback.entity.FeedbackForm;
import com.topleader.topleader.feedback.entity.Question;
import com.topleader.topleader.feedback.entity.Recipient;
import com.topleader.topleader.feedback.repository.FeedbackFormQuestionRepository;
import com.topleader.topleader.feedback.repository.RecipientRepository;
import com.topleader.topleader.common.util.user.UserDetailUtils;
import com.topleader.topleader.user.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.topleader.topleader.common.exception.ErrorCodeConstants.USER_NO_AUTHORIZED;


@Slf4j
@RestController
@RequestMapping("/api/latest/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final FeedbackFormQuestionRepository feedbackFormQuestionRepository;
    private final RecipientRepository recipientRepository;
    private final UserRepository userRepository;

    @GetMapping("/{id}")
    @Secured({"ADMIN", "HR", "COACH", "USER"})
    public FeedbackFormDto getForm(@PathVariable long id) {
        var form = feedbackService.fetchForm(id);
        validate(form.getUsername());

        var user = userRepository.findByUsername(form.getUsername()).orElseThrow();
        var formQuestions = feedbackFormQuestionRepository.findByFeedbackFormId(id);
        var formRecipients = recipientRepository.findByFormId(id);
        var formAnswers = feedbackService.getAnswersByFormId(id);
        var recipientMap = formRecipients.stream()
                .collect(Collectors.toMap(Recipient::getId, r -> r));

        return FeedbackFormDto.witAnswer(form, formQuestions, formRecipients, formAnswers, recipientMap)
                .setFirstName(user.getFirstName())
                .setLastName(user.getLastName());
    }

    @Transactional
    @GetMapping("/user/{username}")
    @Secured({"ADMIN", "HR", "COACH", "USER"})
    public List<FeedbackForms> getForms(@PathVariable String username) {
       var forms = feedbackService.fetchForms(username);
       var formIds = forms.stream().map(com.topleader.topleader.feedback.entity.FeedbackForm::getId).toList();
       var allRecipients = formIds.stream()
               .flatMap(formId -> recipientRepository.findByFormId(formId).stream())
               .collect(Collectors.groupingBy(Recipient::getFormId));
       return FeedbackForms.of(forms, allRecipients);
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

        var form = feedbackService.saveForm(FeedbackFormRequest.toForm(request));

        var formQuestions = FeedbackFormRequest.toQuestions(request.getQuestions(), form.getId());
        feedbackFormQuestionRepository.saveAll(formQuestions);

        // Only save new recipients (those without IDs)
        var newRecipients = FeedbackFormRequest.toRecipients(
                request.getRecipients().stream()
                        .filter(r -> r.id() == null)
                        .toList(),
                form.getId());
        recipientRepository.saveAll(newRecipients);

        return form;
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
    public FeedbackFormDto updateForm(@PathVariable long id, @RequestBody @Valid FeedbackFormRequest request) {
        var savedForm = feedbackService.fetchForm(id);
        validate(savedForm.getUsername());
        var defaultKeys = feedbackService.fetchOptions().stream().map(Question::getKey)
                .collect(Collectors.toList());
        feedbackService.updateQuestions(toQuestions(request.getQuestions(), defaultKeys));

        savedForm.setTitle(request.getTitle());
        savedForm.setValidTo(request.getValidTo());
        savedForm.setDescription(request.getDescription());
        savedForm.setDraft(request.isDraft());
        var form = feedbackService.saveForm(savedForm);

        feedbackFormQuestionRepository.deleteByFeedbackFormId(id);
        feedbackFormQuestionRepository.saveAll(FeedbackFormRequest.toQuestions(request.getQuestions(), id));

        // Keep existing recipients, delete only those not in the request
        var keepIds = request.getRecipients().stream()
                .map(RecipientDto::id)
                .filter(Objects::nonNull)
                .toList();
        if (keepIds.isEmpty()) {
            recipientRepository.deleteByFormId(id);
        } else {
            recipientRepository.deleteByFormIdAndIdNotIn(id, keepIds);
        }
        // Only save new recipients (those without IDs)
        var newRecipients = FeedbackFormRequest.toRecipients(
                request.getRecipients().stream()
                        .filter(r -> r.id() == null)
                        .toList(),
                id);
        log.info("recipient to save: {}", newRecipients);
        recipientRepository.saveAll(newRecipients);

        if (!request.isDraft()) {
            feedbackService.sendFeedbacks(getFeedbackData(request, form));
        }
        return feedbackService.toFeedbackFormDto(form);
    }

    @DeleteMapping("/{id}")
    @Secured({"ADMIN", "HR", "COACH", "USER"})
    public void deleteForm( @PathVariable long id) {
        var savedForm = feedbackService.fetchForm(id);
        validate(savedForm.getUsername());
        feedbackService.deleteForm(id);
    }


    public FeedbackData getFeedbackData(FeedbackFormRequest request, FeedbackForm form) {
        var user = userRepository.findByUsername(form.getUsername()).orElseThrow();
        var byUsername = request.getRecipients().stream()
                .collect(Collectors.toMap(RecipientDto::username, Function.identity()));
        var formRecipients = recipientRepository.findByFormId(form.getId());
        return new FeedbackData().setLocale(request.getLocale())
                .setValidTo(request.getValidTo())
                .setFormId(form.getId())
                .setFirstName(user.getFirstName())
                .setLastName(user.getLastName())
                .setRecipients(formRecipients.stream()
                        .filter(r -> byUsername.containsKey(r.getRecipient())) // Only recipients in request
                        .map(r -> new FeedbackData.Recipient(byUsername.get(r.getRecipient()).id(), r.getRecipient(), r.getToken()))
                        .collect(Collectors.toList()));
    }


    private void validate(String fromUser) {
        var loggedUser = UserDetailUtils.getLoggedUsername();
        if(!fromUser.equals(loggedUser)) {
            throw new ApiValidationException(USER_NO_AUTHORIZED, "user", loggedUser, "User is not allowed to access this form");
        }
    }

}
