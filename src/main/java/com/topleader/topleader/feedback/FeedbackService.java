package com.topleader.topleader.feedback;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.topleader.topleader.email.EmailService;
import com.topleader.topleader.email.VelocityService;
import com.topleader.topleader.feedback.api.FeedbackData;
import com.topleader.topleader.feedback.api.FeedbackFormDto;
import com.topleader.topleader.feedback.entity.FeedbackForm;
import com.topleader.topleader.feedback.entity.FeedbackFormAnswer;
import com.topleader.topleader.feedback.entity.Question;
import com.topleader.topleader.feedback.entity.Recipient;
import com.topleader.topleader.feedback.exception.InvalidFormOrRecipientException;
import com.topleader.topleader.feedback.repository.FeedbackFormAnswerRepository;
import com.topleader.topleader.feedback.repository.FeedbackFormRepository;
import com.topleader.topleader.feedback.repository.QuestionRepository;
import com.topleader.topleader.feedback.repository.RecipientRepository;
import com.topleader.topleader.user.User;
import com.topleader.topleader.user.UserRepository;
import com.topleader.topleader.util.common.user.UserUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import static com.topleader.topleader.util.common.CommonUtils.TOP_LEADER_FORMATTER;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackService {

    private static final Map<String, String> subjects = Map.of(
            "en", "Your Valuable Feedback Requested for %s %s Growth on TopLeader",
            "cs", "Žádost o Vaši cennou zpětnou vazbu pro %s %s na platformě TopLeader",
            "fr", "Demande de Votre Précieux Retour sur les Progrès de %s %s sur TopLeader",
            "de", "Ihre wertvolle Rückmeldung für %s %s's Entwicklung auf TopLeader erbeten");

    private final FeedbackFormRepository feedbackFormRepository;

    private final QuestionRepository questionRepository;

    private final RecipientRepository recipientRepository;

    private final FeedbackFormAnswerRepository feedbackFormAnswerRepository;

    private final VelocityService velocityService;

    private final EmailService emailService;

    private final UserRepository userRepository;

    private final ObjectMapper objectMapper;

    @Value("${top-leader.app-url}")
    private String appUrl;

    @Value("${top-leader.supported-invitations}")
    private List<String> supportedInvitations;

    @Value("${top-leader.default-locale}")
    private String defaultLocale;

    public List<Question> fetchQuestions() {
        return questionRepository.findAll();
    }

    public FeedbackForm fetchForm(long id) {
        return feedbackFormRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Feedback form not found! id: " + id));
    }

    public List<FeedbackForm> fetchForms(String username) {
        return feedbackFormRepository.findByUsername(username);
    }

    @Transactional
    public FeedbackForm saveForm(FeedbackForm form) {
        return feedbackFormRepository.save(form);
    }

    public void deleteForm(long id) {
        feedbackFormRepository.deleteById(id);
    }

    @Transactional
    public Recipient validateRecipientIfValid(long formId, String recipient, String token) {
        return recipientRepository.findByFormIdAndRecipientAndToken(formId, recipient, token)
                .filter(r -> LocalDateTime.now().isBefore(r.getForm().getValidTo().plusDays(1)) && !r.isSubmitted())
                .orElseThrow(() -> new InvalidFormOrRecipientException(String
                        .format("Recipient or form is invalid! formId: %s recipient: %s token %s", formId, recipient, token)));
    }

    @Transactional
    public Recipient validateRecipientIfSubmitted(long formId, String recipient, String token) {
        return recipientRepository.findByFormIdAndRecipientAndToken(formId, recipient, token)
                .filter(r -> LocalDateTime.now().isBefore(r.getForm().getValidTo().plusDays(1)) && r.isSubmitted())
                .orElseThrow(() -> new InvalidFormOrRecipientException(String
                        .format("Recipient or form is invalid! formId: %s recipient: %s token %s", formId, recipient, token)));
    }

    @Transactional
    public List<FeedbackFormAnswer> submitForm(List<FeedbackFormAnswer> answers, String username) {
        userRepository.findById(username)
                .ifPresent(u -> userRepository.save(u.setStatus(User.Status.SUBMITTED)));
        return feedbackFormAnswerRepository.saveAll(answers);
    }

    void sendFeedbacks(FeedbackData data) {
        data.getRecipients().stream()
                .filter(r -> r.id() == null)
                .forEach(r -> {
                    var feedbackLink = String.format("%s/#/feedback/%s/%s/%s", appUrl, data.getFormId(), r.recipient(), r.token());
                    var params = Map.of("validTo", data.getValidTo().format(TOP_LEADER_FORMATTER),
                            "link", feedbackLink, "firstName", data.getFirstName(), "lastName", data.getLastName());
                    var body = velocityService.getMessage(new HashMap<>(params), parseTemplateName(data.getLocale()));
                         var subject = String.format(subjects.getOrDefault(data.getLocale(), defaultLocale), data.getFirstName(), data.getLastName());

                    var newUser = UserUtils.fromEmail(r.recipient())
                            .setAuthorities(Set.of(User.Authority.RESPONDENT))
                            .setStatus(User.Status.REQUESTED);
                    userRepository.save(newUser);
                    emailService.sendEmail(r.recipient(), subject, body);
                });
    }

    public String parseTemplateName(String locale) {
        return "templates/feedback/feedback-" + parseLocale(locale) + ".vm";
    }

    public String parseLocale(String locale) {
        return supportedInvitations.contains(locale) ? locale : defaultLocale;
    }


    @Transactional
    public FeedbackFormDto toFeedbackFormDto(FeedbackForm form) {
        return FeedbackFormDto.of(form);
    }


}
