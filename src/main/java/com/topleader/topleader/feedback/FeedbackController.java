package com.topleader.topleader.feedback;


import com.topleader.topleader.feedback.api.FeedbackFormDto;
import com.topleader.topleader.feedback.api.FeedbackFormOptions;
import com.topleader.topleader.feedback.api.FeedbackFormRequest;
import com.topleader.topleader.feedback.entity.*;

import com.topleader.topleader.user.User;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/latest/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @Transactional
    @GetMapping("/options")
    @Secured({"ADMIN", "HR", "COACH", "USER"})
    public FeedbackFormOptions getOptions() {
        return FeedbackFormOptions.of(feedbackService.fetchQuestions());
    }

    @Transactional
    @GetMapping("/{id}")
    @Secured({"ADMIN", "HR", "COACH", "USER"})
    public FeedbackFormDto getForm(@PathVariable long id) {
        return FeedbackFormDto.of(feedbackService.fetch(id));
    }

    @Transactional
    @PostMapping
    @Secured({"ADMIN", "HR", "COACH", "USER"})
    public FeedbackFormDto createForm(@RequestBody @Valid FeedbackFormRequest request) {
        var form = feedbackService.save(FeedbackFormRequest.toForm(request));
        return FeedbackFormDto.of(form);
    }

    @Transactional
    @PutMapping("/{id}")
    @Secured({"ADMIN", "HR", "COACH", "USER"})
    public FeedbackFormDto updaateForm(@PathVariable long id,  @RequestBody @Valid FeedbackFormRequest request) {
        var form = FeedbackFormRequest.toForm(request).setId(id);
        return FeedbackFormDto.of(feedbackService.save(form));
    }

    @DeleteMapping("/{id}")
    @Secured({"ADMIN", "HR", "COACH", "USER"})
    public void deleteForm(@PathVariable long id) {
        feedbackService.delete(id);
    }

}
