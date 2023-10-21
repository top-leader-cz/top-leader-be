package com.topleader.topleader.feedback;

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
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackFormRepository feedbackFormRepository;

    private final QuestionRepository questionRepository;

    private final RecipientRepository recipientRepository;

    private final FeedbackFormAnswerRepository feedbackFormAnswerRepository;

    public List<Question> fetchQuestions() {
        return questionRepository.findAll();
    }

    public FeedbackForm fetchForm(long id) {
        return feedbackFormRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Feedback form not found! id: " + id));
    }

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
//        recipientRepository.findById(recipientId)
//                .ifPresent(r -> recipientRepository.save(r.setSubmitted(true)));
    }

}
