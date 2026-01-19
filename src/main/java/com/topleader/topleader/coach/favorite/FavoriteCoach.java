package com.topleader.topleader.coach.favorite;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.relational.core.mapping.Table;
import com.topleader.topleader.common.entity.BaseEntity;

@Data
@EqualsAndHashCode(callSuper=false)
@Table("favorite_coach")
@NoArgsConstructor
@Accessors(chain = true)
public class FavoriteCoach extends BaseEntity {
    private String username;

    private String coachUsername;
}
