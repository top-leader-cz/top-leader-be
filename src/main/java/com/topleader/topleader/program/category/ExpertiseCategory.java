package com.topleader.topleader.program.category;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Set;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@Table("expertise_category")
public class ExpertiseCategory {

    @Id
    private String key;

    private String name;

    private Set<String> coachFields = Set.of();
}
