package com.topleader.topleader.common.ai;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("ai_prompt")
public class AiPrompt {

    @Id
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
        IMAGE_MATCH,
    }

}
