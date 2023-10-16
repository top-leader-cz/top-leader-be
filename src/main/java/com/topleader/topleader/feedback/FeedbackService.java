package com.topleader.topleader.feedback;

import com.topleader.topleader.feedback.entity.FeedbackForm;
import com.topleader.topleader.feedback.entity.Question;
import com.topleader.topleader.feedback.repository.FeedbackFormRepository;
import com.topleader.topleader.feedback.repository.QuestionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackFormRepository feedbackFormRepository;

    private final QuestionRepository questionRepository;



    public List<Question> fetchQuestions() {
        return questionRepository.findAll();
    }

    public FeedbackForm fetch(long id) {
        return feedbackFormRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Feedback form not found! id: " + id));
    }

    public FeedbackForm create(FeedbackForm form) {
        return feedbackFormRepository.save(form);
    }
}
