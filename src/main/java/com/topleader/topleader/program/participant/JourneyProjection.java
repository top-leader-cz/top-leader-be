package com.topleader.topleader.program.participant;

import org.springframework.data.annotation.Id;

public record JourneyProjection(
        @Id Long participantId,
        String focusArea,
        String personalGoal,
        String programName,
        boolean isFinalCycle,

        // assessments (baseline)
        Integer baselineQ1, Integer baselineQ2, Integer baselineQ3, Integer baselineQ4, Integer baselineQ5,
        // assessments (mid) — nullable
        Integer midQ1, Integer midQ2, Integer midQ3, Integer midQ4, Integer midQ5,
        // assessments (final)
        Integer finalQ1, Integer finalQ2, Integer finalQ3, Integer finalQ4, Integer finalQ5,

        // allocation — nullable
        Integer allocatedUnits, Integer consumedUnits,

        // practice counts
        long practicesTotal, long practicesResponded
) {}
