package com.topleader.topleader.feedback.api;

import com.topleader.topleader.feedback.entity.Question;

import java.util.List;

public record QuestionDto(String key, QuestionType type, boolean required, List<AnswerRecipientDto> answers) {

}

