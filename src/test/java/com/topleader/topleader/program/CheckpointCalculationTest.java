package com.topleader.topleader.program;

import com.topleader.topleader.program.dto.CheckpointDto;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CheckpointCalculationTest {

    @Test
    void noCheckpoints_whenDurationIsNull() {
        var result = ProgramController.computeCheckpoints(null, null);
        assertThat(result).isEmpty();
    }

    @Test
    void noCheckpoints_whenDurationIsZero() {
        var result = ProgramController.computeCheckpoints(0, null);
        assertThat(result).isEmpty();
    }

    @Test
    void singleCycle_whenNoCycleLengthDays() {
        // 90 day program, no cycle -> enrollment + mid(45) + final(90)
        var result = ProgramController.computeCheckpoints(90, null);
        assertThat(result).hasSize(3);
        assertThat(result.get(0)).isEqualTo(new CheckpointDto("Enrollment", 0));
        assertThat(result.get(1)).isEqualTo(new CheckpointDto("Mid-cycle", 45));
        assertThat(result.get(2)).isEqualTo(new CheckpointDto("Final review", 90));
    }

    @Test
    void multipleCycles_30daysCycleIn90dayProgram() {
        // 90 days / 30 day cycles -> enrollment, mid(15), review(30), mid(45), review(60), mid(75), final(90)
        var result = ProgramController.computeCheckpoints(90, 30);
        assertThat(result).hasSize(7);
        assertThat(result.get(0)).isEqualTo(new CheckpointDto("Enrollment", 0));
        assertThat(result.get(1)).isEqualTo(new CheckpointDto("Mid-cycle", 15));
        assertThat(result.get(2)).isEqualTo(new CheckpointDto("Cycle review", 30));
        assertThat(result.get(3)).isEqualTo(new CheckpointDto("Mid-cycle", 45));
        assertThat(result.get(4)).isEqualTo(new CheckpointDto("Cycle review", 60));
        assertThat(result.get(5)).isEqualTo(new CheckpointDto("Mid-cycle", 75));
        assertThat(result.get(6)).isEqualTo(new CheckpointDto("Final review", 90));
    }

    @Test
    void cycleLengthEqualsDuration() {
        // 90 days / 90 day cycle -> enrollment, mid(45), final(90)
        var result = ProgramController.computeCheckpoints(90, 90);
        assertThat(result).hasSize(3);
        assertThat(result.get(0)).isEqualTo(new CheckpointDto("Enrollment", 0));
        assertThat(result.get(1)).isEqualTo(new CheckpointDto("Mid-cycle", 45));
        assertThat(result.get(2)).isEqualTo(new CheckpointDto("Final review", 90));
    }

    @Test
    void shortProgram_14days() {
        var result = ProgramController.computeCheckpoints(14, 7);
        assertThat(result).hasSize(5);
        assertThat(result.get(0)).isEqualTo(new CheckpointDto("Enrollment", 0));
        assertThat(result.get(1)).isEqualTo(new CheckpointDto("Mid-cycle", 3));
        assertThat(result.get(2)).isEqualTo(new CheckpointDto("Cycle review", 7));
        assertThat(result.get(3)).isEqualTo(new CheckpointDto("Mid-cycle", 10));
        assertThat(result.get(4)).isEqualTo(new CheckpointDto("Final review", 14));
    }

    @Test
    void zeroCycleLengthDays_treatedAsSingleCycle() {
        var result = ProgramController.computeCheckpoints(90, 0);
        assertThat(result).hasSize(3);
        assertThat(result.get(0)).isEqualTo(new CheckpointDto("Enrollment", 0));
        assertThat(result.get(1)).isEqualTo(new CheckpointDto("Mid-cycle", 45));
        assertThat(result.get(2)).isEqualTo(new CheckpointDto("Final review", 90));
    }
}
