package com.topleader.topleader.user.session.feedback;


import com.fasterxml.jackson.core.type.TypeReference;
import com.topleader.topleader.common.util.common.JsonUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Map;
import com.topleader.topleader.common.entity.BaseEntity;

@Table("session_feedback")
@Data
@EqualsAndHashCode(callSuper=false)
@Accessors(chain = true)
public class SessionFeedback extends BaseEntity {
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
