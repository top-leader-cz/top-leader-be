package com.topleader.topleader.user.session.feedback;


import com.fasterxml.jackson.core.type.TypeReference;
import com.topleader.topleader.common.util.common.JsonUtils;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Map;

@Table("session_feedback")
@Data
@Accessors(chain = true)
public class SessionFeedback {

    @Id
    private Long id;

    private Long sessionId;

    private String username;

    private String answers;

    private String feedback;

    public Map<String, Integer> getAnswers() {
        if (answers == null) {
            return Map.of();
        }
        return JsonUtils.fromJsonString(answers, new TypeReference<Map<String, Integer>>() {});
    }

    public SessionFeedback setAnswers(Map<String, Integer> answers) {
        this.answers = answers != null ? JsonUtils.toJsonString(answers) : null;
        return this;
    }
}
