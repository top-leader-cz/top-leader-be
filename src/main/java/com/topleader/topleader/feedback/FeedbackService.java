package com.topleader.topleader.feedback;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.topleader.topleader.ai.AiClient;
import com.topleader.topleader.email.EmailService;
import com.topleader.topleader.email.VelocityService;
import com.topleader.topleader.exception.ApiValidationException;
import com.topleader.topleader.feedback.api.*;
import com.topleader.topleader.feedback.entity.FeedbackForm;
import com.topleader.topleader.feedback.entity.FeedbackFormAnswer;
import com.topleader.topleader.feedback.entity.Question;
import com.topleader.topleader.feedback.entity.Recipient;
import com.topleader.topleader.feedback.repository.FeedbackFormAnswerRepository;
import com.topleader.topleader.feedback.repository.FeedbackFormRepository;
import com.topleader.topleader.feedback.repository.QuestionRepository;
import com.topleader.topleader.feedback.repository.RecipientRepository;
import com.topleader.topleader.user.User;
import com.topleader.topleader.user.UserRepository;
import com.topleader.topleader.util.common.FileUtils;
import com.topleader.topleader.util.common.Translation;
import com.topleader.topleader.util.common.TranslationUtils;
import com.topleader.topleader.util.common.user.UserUtils;
import io.vavr.control.Try;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.topleader.topleader.exception.ErrorCodeConstants.FROM_ALREADY_SUBMITTED;
import static com.topleader.topleader.user.User.Status.*;
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

    private final AiClient aiClient;

    private final ObjectMapper objectMapper;

    @Value("${top-leader.app-url}")
    private String appUrl;

    @Value("${top-leader.supported-invitations}")
    private List<String> supportedInvitations;

    @Value("${top-leader.default-locale}")
    private String defaultLocale;

    @Value("${top-leader.feedback.summary-limit}")
    private int summaryLimit;

    public List<Question> fetchOptions() {
        return questionRepository.getDefaultOptions();
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

    public void updateQuestions(List<Question> questions) {
        questionRepository.saveAll(questions);
    }

    public void deleteForm(long id) {
        feedbackFormRepository.deleteById(id);
    }

    @Transactional
    public Recipient validateRecipientIfValid(long formId, String recipient, String token) {
        return recipientRepository.findByFormIdAndRecipientAndToken(formId, recipient, token)
                .filter(r -> LocalDateTime.now().isBefore(r.getForm().getValidTo().plusDays(1)) && !r.isSubmitted())
                .orElseThrow(() -> new ApiValidationException(FROM_ALREADY_SUBMITTED, "user", recipient, String
                        .format("Recipient or form is invalid! formId: %s recipient: %s token %s", formId, recipient, token)));
    }

    @Transactional
    public void validateRecipientIfSubmitted(long formId, String recipient, String token) {
        recipientRepository.findByFormIdAndRecipientAndToken(formId, recipient, token)
                .filter(r -> LocalDateTime.now().isBefore(r.getForm().getValidTo().plusDays(1)) && r.isSubmitted())
                .orElseThrow(() -> new ApiValidationException(FROM_ALREADY_SUBMITTED, "user", recipient, String
                        .format("Recipient or form is invalid! formId: %s recipient: %s token %s", formId, recipient, token)));
    }

    @Transactional
    public List<FeedbackFormAnswer> submitForm(List<FeedbackFormAnswer> answers, String username) {
        userRepository.findById(username)
                .ifPresent(u -> {
                    if (skipUpdate(u)) return;
                    userRepository.save(u.setStatus(User.Status.SUBMITTED));
                });
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

                    var testedUser = userRepository.findById(r.recipient()).orElse(new User());
                    if (!skipUpdate(testedUser)) {
                        var newUser = UserUtils.fromEmail(r.recipient())
                                .setAuthorities(Set.of(User.Authority.RESPONDENT))
                                .setStatus(User.Status.REQUESTED);
                        userRepository.save(newUser);
                    }

                    emailService.sendEmail(r.recipient(), subject, body);
                });
    }

    @SneakyThrows
    @Transactional
    public void generateSummary(long formId) {
        var form = feedbackFormRepository.getReferenceById(formId);
        var user = form.getUser();
        var formDto = FeedbackFormDto.witAnswer(form);
        var translations = TranslationUtils.getTranslation();
        var questions = formDto.getQuestions().stream()
                .collect(Collectors.toMap(q -> TranslationUtils.translate(q.key(), translations), q -> q.answers().stream()
                        .map(AnswerRecipientDto::answer)
                        .collect(Collectors.toList())));

        if (formDto.allowSummary(summaryLimit)) {
            var summary = Try.of(() -> objectMapper.readValue(aiClient.generateSummary(UserUtils.localeToLanguage(user.getLocale()), questions), Summary.class))
                    .onFailure(e -> log.info("Failed to generate summary for form: [{}] ", formId, e))
                            .getOrElse(new Summary());
            form.setSummary(summary);
            feedbackFormRepository.save(form);
        }
    }

    boolean skipUpdate(User u) {
        return AUTHORIZED == u.getStatus() || PAID == u.getStatus() || PENDING == u.getStatus();
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
