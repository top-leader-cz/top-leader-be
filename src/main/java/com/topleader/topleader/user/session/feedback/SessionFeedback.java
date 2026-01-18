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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sessionId;

    private String username;

    @Convert(converter = SessionFeedbackAnswerConverter.class)
    private Map<String, Integer> answers;

    private String feedback;
}
