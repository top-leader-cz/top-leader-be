package com.topleader.topleader.coach;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@Accessors(chain = true)
@Table("coach_fields")
public class CoachField {

    private String coachFields;

    public CoachField(String field) {
        this.coachFields = field;
    }
}
