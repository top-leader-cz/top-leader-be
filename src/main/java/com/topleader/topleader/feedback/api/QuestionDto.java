package com.topleader.topleader.feedback.api;

import com.topleader.topleader.feedback.entity.Question;

public record QuestionDto(String key, Question.Type type, boolean required) {
}