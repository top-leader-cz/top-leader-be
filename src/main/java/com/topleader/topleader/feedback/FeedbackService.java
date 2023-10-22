package com.topleader.topleader.feedback;

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
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.topleader.topleader.util.common.CommonUtils.TOP_LEADER_FORMATTER;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private static final Map<String, String> subjects = Map.of(
            "en", "Your Valuable Feedback Requested for %s Growth on TopLeader",
            "cs", "Žádost o Vaši cennou zpětnou vazbu pro %s na platformě TopLeader",
            "fr", "Demande de Votre Précieux Retour sur les Progrès de %s sur TopLeader");

    private final FeedbackFormRepository feedbackFormRepository;

    private final QuestionRepository questionRepository;

    private final RecipientRepository recipientRepository;

    private final FeedbackFormAnswerRepository feedbackFormAnswerRepository;

    private final VelocityService velocityService;

    private final EmailService emailService;

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
    public Recipient getRecipientIfValid(long formId, String recipient, String token) {
        return recipientRepository.findByFormIdAndRecipientAndToken(formId, recipient, token)
                .filter(r -> LocalDateTime.now().isBefore(r.getForm().getValidTo()) && !r.isSubmitted())
                .orElseThrow(() -> new InvalidFormOrRecipientException("Recipient or form is invalid!"));
    }

    @Transactional
    public List<FeedbackFormAnswer> submitForm(List<FeedbackFormAnswer> answers) {
        return feedbackFormAnswerRepository.saveAll(answers);
    }

    public void sendFeedbacks(FeedbackData data) {
        data.getRecipients().stream()
                .filter(r -> r.id() == null)
                .forEach(r -> {
                    var feedbackLink = String.format("%s/#/feedback/%s/%s/%s", appUrl, data.getFormId(), r.recipient(), r.token());
                    var params = Map.of("validTo", data.getValidTo().format(TOP_LEADER_FORMATTER), "link", feedbackLink);
                    var body = velocityService.getMessage(new HashMap<>(params), parseTemplateName(data.getLocale()));
                    var subject = String.format(subjects.get(data.getLocale()), r.recipient());

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
