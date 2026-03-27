package com.topleader.topleader.program.template;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@Table("program_template")
public class ProgramTemplate {

    @Id
    private Long id;

    private Long companyId;

    private String name;

    private String description;

    private String goal;

    private String targetGroup;

    private Integer durationDays;

    private Set<String> focusAreas = Set.of();

    private boolean active = true;

    private LocalDateTime createdAt;

    private String createdBy;

    private LocalDateTime updatedAt;

    private String updatedBy;
}
