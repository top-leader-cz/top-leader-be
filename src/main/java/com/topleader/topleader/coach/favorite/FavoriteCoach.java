package com.topleader.topleader.coach.favorite;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("favorite_coach")
@NoArgsConstructor
@Accessors(chain = true)
public class FavoriteCoach {

    @Id
    private Long id;

    private String username;

    private String coachUsername;
}
