package com.topleader.topleader.coach.favorite;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Entity
@Accessors(chain = true)
public class FavoriteCoach {

    @EmbeddedId
    private FavoriteCoachId id;

    @Column(insertable = false, updatable = false)
    private String username;

    @Data
    @Embeddable
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FavoriteCoachId implements Serializable {

        @Column(name = "username")
        private String username;

        @Column(name = "coach_username")
        private String coachUsername;
    }
}
