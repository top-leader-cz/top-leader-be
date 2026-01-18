package com.topleader.topleader.coach.note;

import com.topleader.topleader.coach.favorite.FavoriteCoach;
import jakarta.persistence.*;

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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String coachId;

    private String userId;

    private String note;

    private LocalDateTime createdAt = LocalDateTime.now();
}
