package com.topleader.topleader.program.participant.practice;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@Table("weekly_practice")
public class WeeklyPractice {

    public enum Source {
        AI, EDITED, CUSTOM
    }

    public enum FridayResponse {
        YES, PARTIAL, NO
    }

    @Id
    private Long id;

    private Long participantId;

    private int cycle;

    private int weekNumber;

    private String text;

    private Source source;

    private FridayResponse fridayResponse;

    private String blockerReason;

    private LocalDateTime createdAt;
}
