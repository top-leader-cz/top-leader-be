package com.topleader.topleader.coach.note;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import com.topleader.topleader.common.entity.BaseEntity;

@Table("coach_user_note")
@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@Accessors(chain = true)
public class CoachUserNote extends BaseEntity {
    private String coachId;

    private String userId;

    private String note;

    private LocalDateTime createdAt = LocalDateTime.now();
}
