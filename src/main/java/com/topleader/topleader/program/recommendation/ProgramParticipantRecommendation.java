package com.topleader.topleader.program.recommendation;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@Table("program_participant_recommendation")
public class ProgramParticipantRecommendation {

    public enum Type {
        ARTICLE, VIDEO
    }

    @Id
    private Long id;
    private Long programParticipantId;
    private Integer cycle;
    private Type type;
    private String content;
    private Integer relevanceRank;
    private LocalDateTime createdAt;
}
