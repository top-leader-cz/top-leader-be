package com.topleader.topleader.ai;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class AiPrompt {

    @Id
    @Enumerated(EnumType.STRING)
    private PromptType id;

    private String value;

    public enum PromptType {
        LEADERSHIP_STYLE,
        ANIMAL_SPIRIT,
        LEADERSHIP_TIP,
        PERSONAL_GROWTH_TIP,
        WORLD_LEADER_PERSONA,
        LONG_TERM_GOALS,
        ACTIONS_STEPS,
        FEEDBACK_SUMMARY,
        USER_PREVIEWS,
        RECOMMENDED_GROWTH,
        USER_ARTICLES,
        SUGGESTION,
    }

}
