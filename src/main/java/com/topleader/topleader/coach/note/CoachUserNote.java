package com.topleader.topleader.coach.note;

import com.topleader.topleader.coach.favorite.FavoriteCoach;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Data
@Accessors(chain = true)
public class CoachUserNote {

    @EmbeddedId
    private CoachUserNoteId id;

    private String note;

    private LocalDateTime createdAt = LocalDateTime.now();


    @Data
    @Embeddable
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CoachUserNoteId implements Serializable {

        private String coachId;

        private String userId;
    }
}
