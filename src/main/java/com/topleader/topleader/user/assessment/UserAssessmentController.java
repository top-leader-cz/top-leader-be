/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.user.assessment;

import com.topleader.topleader.exception.NotFoundException;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author Daniel Slavik
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/latest/user-assessments")
public class UserAssessmentController {

    private final UserAssessmentRepository userAssessmentRepository;

    @GetMapping
    public UserAssessmentDto getUserAssessments(@AuthenticationPrincipal UserDetails user) {
        return UserAssessmentDto.from(userAssessmentRepository.findAllByUsername(user.getUsername()));
    }

    @DeleteMapping
    @Transactional
    public void deleteUserAssessments(@AuthenticationPrincipal UserDetails user) {
        userAssessmentRepository.deleteAllByUsername(user.getUsername());
    }

    @GetMapping("/{assessmentId}")
    public AnsweredQuestionDto getUserAssessments(
        @AuthenticationPrincipal UserDetails user,
        @PathVariable Long assessmentId
    ) {

        return userAssessmentRepository.findById(new UserAssessmentId()
                .setUsername(user.getUsername())
                .setQuestionId(assessmentId)
            )
            .map(AnsweredQuestionDto::from)
            .orElseThrow(NotFoundException::new);
    }

    @PostMapping("/{assessmentId}")
    public AnsweredQuestionDto setUserAssessments(
        @AuthenticationPrincipal UserDetails user,
        @PathVariable Long assessmentId,
        @RequestBody SetAssessmentRequest request
    ) {

        return AnsweredQuestionDto.from(
            userAssessmentRepository.save(new UserAssessment()
                .setUsername(user.getUsername())
                .setQuestionId(assessmentId)
                .setAnswer(request.answer())
            )
        );
    }

    public record SetAssessmentRequest(Integer answer) {
    }

    public record UserAssessmentDto(
        Integer questionAnswered,
        List<AnsweredQuestionDto> answers
    ) {

        public static UserAssessmentDto from(List<UserAssessment> assessments) {
            return new UserAssessmentDto(
                assessments.size(),
                assessments.stream()
                    .map(AnsweredQuestionDto::from)
                    .toList()
            );
        }

    }

    public record AnsweredQuestionDto(
        Long questionId,
        Integer answer
    ) {
        public static AnsweredQuestionDto from(UserAssessment a) {
            return new AnsweredQuestionDto(a.getQuestionId(), a.getAnswer());
        }

    }
}
