package com.topleader.topleader.coach;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@Accessors(chain = true)
@Table("coach_languages")
public class CoachLanguage {

    private String coachLanguages;

    public CoachLanguage(String language) {
        this.coachLanguages = language;
    }
}
