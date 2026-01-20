package com.topleader.topleader.coach.note;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("coach_user_note")
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class CoachUserNote {

    @Id
    private Long id;

    private String coachUsername;

    private String username;

    private String note;

    private LocalDateTime createdAt = LocalDateTime.now();
}
