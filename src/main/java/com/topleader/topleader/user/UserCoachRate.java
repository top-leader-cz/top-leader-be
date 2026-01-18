package com.topleader.topleader.user;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@Accessors(chain = true)
@Table("user_coach_rates")
public class UserCoachRate {

    private String rateName;

    public UserCoachRate(String rateName) {
        this.rateName = rateName;
    }
}
