package com.topleader.topleader.user.session.feedback;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Map;

@Entity
@Data
@Accessors(chain = true)
public class SessionFeedback {

    @EmbeddedId
    private SessionFeedbackId id;

    @Convert(converter = SessionFeedbackAnswerConverter.class)
    private Map<String, Integer> answers;

    private String feedback;

    @Data
    @Embeddable
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SessionFeedbackId implements Serializable {

        @Column(name = "session_id")
        private Long sessionId;

        @Column(name = "username")
        private String username;

    }

}
