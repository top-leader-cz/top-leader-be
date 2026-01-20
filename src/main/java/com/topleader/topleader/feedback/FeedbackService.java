package com.topleader.topleader.feedback;

import com.topleader.topleader.common.ai.AiClient;
import com.topleader.topleader.common.email.EmailService;
import com.topleader.topleader.common.email.TemplateService;
import com.topleader.topleader.common.exception.ApiValidationException;
import com.topleader.topleader.feedback.api.*;
import com.topleader.topleader.feedback.entity.FeedbackForm;
import com.topleader.topleader.feedback.entity.FeedbackFormAnswer;
import com.topleader.topleader.feedback.entity.Question;
import com.topleader.topleader.feedback.entity.Recipient;
import com.topleader.topleader.feedback.feedback_notification.FeedbackNotificationService;
import com.topleader.topleader.feedback.repository.FeedbackFormAnswerRepository;
import com.topleader.topleader.feedback.repository.FeedbackFormQuestionRepository;
import com.topleader.topleader.feedback.repository.FeedbackFormRepository;
import com.topleader.topleader.feedback.repository.QuestionRepository;
import com.topleader.topleader.feedback.repository.RecipientRepository;
import com.topleader.topleader.user.User;
import com.topleader.topleader.user.UserRepository;
import com.topleader.topleader.common.util.common.TranslationUtils;
import com.topleader.topleader.common.util.common.user.UserUtils;
import com.topleader.topleader.common.util.common.CommonUtils;
import com.topleader.topleader.common.exception.NotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.topleader.topleader.common.exception.ErrorCodeConstants.FROM_ALREADY_SUBMITTED;
import static com.topleader.topleader.user.User.Status.*;
import static com.topleader.topleader.common.util.common.CommonUtils.TOP_LEADER_FORMATTER;

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

    private final FeedbackFormQuestionRepository feedbackFormQuestionRepository;

    private final QuestionRepository questionRepository;

    private final RecipientRepository recipientRepository;

    private final FeedbackFormAnswerRepository feedbackFormAnswerRepository;

    private final TemplateService velocityService;

    private final EmailService emailService;

    private final UserRepository userRepository;

    private final AiClient aiClient;

    private final JsonMapper jsonMapper;

    private final FeedbackNotificationService feedbackNotificationService;

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
                .orElseThrow(() -> new NotFoundException());
    }

    public List<FeedbackForm> fetchForms(String username) {
        return feedbackFormRepository.findByUsername(username);
    }

    public List<FeedbackFormAnswer> getAnswersByFormId(long formId) {
        return feedbackFormAnswerRepository.findByFormId(formId);
    }

    public FeedbackForm saveForm(FeedbackForm form) {
        return feedbackFormRepository.save(form);
    }

    public void updateQuestions(List<Question> questions) {
        var newQuestions = questions.stream()
                .filter(q -> !questionRepository.existsByKey(q.getKey()))
                .toList();
        questionRepository.saveAll(newQuestions);
    }

    public void deleteForm(long id) {
        feedbackFormRepository.deleteById(id);
    }

    public Recipient validateRecipientIfValid(long formId, String recipient, String token) {
        var recipientEntity = recipientRepository.findByFormIdAndRecipientAndToken(formId, recipient, token)
                .orElseThrow(() -> new ApiValidationException(FROM_ALREADY_SUBMITTED, "user", recipient, String
                        .format("Recipient or form is invalid! formId: %s recipient: %s token %s", formId, recipient, token)));

        var form = feedbackFormRepository.findById(formId)
                .orElseThrow(NotFoundException::new);

        if (LocalDateTime.now().isAfter(form.getValidTo().plusDays(1)) || recipientEntity.isSubmitted()) {
            throw new ApiValidationException(FROM_ALREADY_SUBMITTED, "user", recipient, String
                    .format("Recipient or form is invalid! formId: %s recipient: %s token %s", formId, recipient, token));
        }

        return recipientEntity;
    }

    public void validateRecipientIfSubmitted(long formId, String recipient, String token) {
        var recipientEntity = recipientRepository.findByFormIdAndRecipientAndToken(formId, recipient, token)
                .orElseThrow(() -> new ApiValidationException(FROM_ALREADY_SUBMITTED, "user", recipient, String
                        .format("Recipient or form is invalid! formId: %s recipient: %s token %s", formId, recipient, token)));

        var form = feedbackFormRepository.findById(formId)
                .orElseThrow(NotFoundException::new);

        if (LocalDateTime.now().isAfter(form.getValidTo().plusDays(1)) || !recipientEntity.isSubmitted()) {
            throw new ApiValidationException(FROM_ALREADY_SUBMITTED, "user", recipient, String
                    .format("Recipient or form is invalid! formId: %s recipient: %s token %s", formId, recipient, token));
        }
    }

    @Transactional
    public List<FeedbackFormAnswer> submitForm(List<FeedbackFormAnswer> answers, String username, Recipient recipient) {
        userRepository.findByUsername(username)
                .ifPresent(u -> {
                    if (skipUpdate(u)) return;
                    userRepository.save(u.setStatus(User.Status.SUBMITTED));
                });
        recipientRepository.save(recipient);
        var saved = feedbackFormAnswerRepository.saveAll(answers);
        return java.util.stream.StreamSupport.stream(saved.spliterator(), false).toList();
    }

    void sendFeedbacks(FeedbackData data) {
        data.getRecipients().stream()
                .filter(r -> r.id() == null)
                .forEach(r -> {
                    var feedbackLink = String.format("%s/#/feedback/%s/%s/%s", appUrl, data.getFormId(), r.recipient(), r.token());
                    var validTo = Optional.ofNullable(data.getValidTo())
                            .map(v -> v.format(TOP_LEADER_FORMATTER))
                            .orElse(null);
                    var params = Map.of("validTo", validTo,
                            "link", feedbackLink, "firstName", data.getFirstName(), "lastName", data.getLastName());
                    var body = velocityService.getMessage(new HashMap<>(params), parseTemplateName(data.getLocale()));
                    var subject = String.format(subjects.getOrDefault(data.getLocale(), defaultLocale), data.getFirstName(), data.getLastName());

                    var existingUser = userRepository.findByUsernameOrEmail(r.recipient());
                    if (existingUser.isEmpty()) {
                        var newUser = UserUtils.fromEmail(r.recipient())
                                .setAuthorities(Set.of(User.Authority.RESPONDENT))
                                .setStatus(User.Status.REQUESTED);
                        userRepository.save(newUser);
                    }

                    emailService.sendEmail(r.recipient(), subject, body);
                });

        feedbackNotificationService.registerNotification(data.getFormId());
    }

    @SneakyThrows
    public void generateSummary(long formId) {
        log.info("Generating summary for form: [{}]", formId);
        var form = feedbackFormRepository.findById(formId)
                .orElseThrow(() -> new NotFoundException());
        var user = userRepository.findByUsername(form.getUsername()).orElseThrow();

        var formQuestions = feedbackFormQuestionRepository.findByFeedbackFormId(formId);
        var formRecipients = recipientRepository.findByFormId(formId);
        var formAnswers = feedbackFormAnswerRepository.findByFormId(formId);
        var recipientMap = formRecipients.stream()
                .collect(Collectors.toMap(Recipient::getId, r -> r));

        var formDto = FeedbackFormDto.witAnswer(form, formQuestions, formRecipients, formAnswers, recipientMap);
        var translations = TranslationUtils.getTranslation();
        var questions = formDto.getQuestions().stream()
                .collect(Collectors.toMap(q -> TranslationUtils.translate(q.key(), translations), q -> q.answers().stream()
                        .map(AnswerRecipientDto::answer)
                        .collect(Collectors.toList())));
        log.info("Generating summary for form: [{}] with questions: [{}]", formId, questions);

        if (formDto.allowSummary(summaryLimit)) {
            var summary = CommonUtils.tryGetOrElse(
                    () -> jsonMapper.readValue(aiClient.generateSummary(UserUtils.localeToLanguage(user.getLocale()), questions), Summary.class),
                    new Summary(),
                    "Failed to generate summary for form: [" + formId + "]");
            form.setSummary(summary);
            feedbackFormRepository.save(form);
        }
    }

    boolean skipUpdate(User u) {
        return AUTHORIZED == u.getStatus() || PAID == u.getStatus() || PENDING == u.getStatus();
    }


    public String parseTemplateName(String locale) {
        return "templates/feedback/feedback-" + parseLocale(locale) + ".html";
    }

    public String parseLocale(String locale) {
        return supportedInvitations.contains(locale) ? locale : defaultLocale;
    }


    public FeedbackFormDto toFeedbackFormDto(FeedbackForm form) {
        var formQuestions = feedbackFormQuestionRepository.findByFeedbackFormId(form.getId());
        var formRecipients = recipientRepository.findByFormId(form.getId());
        var user = userRepository.findByUsername(form.getUsername()).orElseThrow();
        return FeedbackFormDto.of(form, formQuestions, formRecipients)
                .setFirstName(user.getFirstName())
                .setLastName(user.getLastName());
    }


}
