package com.topleader.topleader.hr.program;

import com.topleader.topleader.hr.program.dto.ParticipantDto;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ParticipantStatusTest {

    private static ProgramRepository.ParticipantRow row(int consumed, int allocated) {
        return new ProgramRepository.ParticipantRow("user1", "John", "Doe", null, null, null, consumed, allocated);
    }

    @Test
    void notStarted_whenValidFromIsNull() {
        var dto = ParticipantDto.from(row(0, 5), null, LocalDateTime.now().plusDays(30));
        assertThat(dto.status()).isEqualTo(ParticipantDto.ParticipantStatus.NOT_STARTED);
    }

    @Test
    void notStarted_whenMilestoneDateIsNull() {
        var dto = ParticipantDto.from(row(0, 5), LocalDateTime.now().minusDays(10), null);
        assertThat(dto.status()).isEqualTo(ParticipantDto.ParticipantStatus.NOT_STARTED);
    }

    @Test
    void notStarted_whenProgramNotYetStarted() {
        var validFrom = LocalDateTime.now().plusDays(5);
        var milestone = LocalDateTime.now().plusDays(95);
        var dto = ParticipantDto.from(row(0, 5), validFrom, milestone);
        assertThat(dto.status()).isEqualTo(ParticipantDto.ParticipantStatus.NOT_STARTED);
    }

    @Test
    void onTrack_whenConsumedMatchesPace() {
        // 90 day program, 60 days elapsed -> 66% elapsed
        // allocated=6, threshold = 6 * (60/90) * 0.8 = 3.2
        // consumed=4 >= 3.2 -> ON_TRACK
        var validFrom = LocalDateTime.now().minusDays(60);
        var milestone = LocalDateTime.now().plusDays(30);
        var dto = ParticipantDto.from(row(4, 6), validFrom, milestone);
        assertThat(dto.status()).isEqualTo(ParticipantDto.ParticipantStatus.ON_TRACK);
    }

    @Test
    void atRisk_whenConsumedBelowThreshold() {
        // 90 day program, 60 days elapsed -> 66% elapsed
        // allocated=6, threshold = 6 * (60/90) * 0.8 = 3.2
        // consumed=1 < 3.2 -> AT_RISK
        var validFrom = LocalDateTime.now().minusDays(60);
        var milestone = LocalDateTime.now().plusDays(30);
        var dto = ParticipantDto.from(row(1, 6), validFrom, milestone);
        assertThat(dto.status()).isEqualTo(ParticipantDto.ParticipantStatus.AT_RISK);
    }

    @Test
    void onTrack_whenZeroAllocated() {
        // threshold = 0 * anything = 0, consumed=0 >= 0 -> ON_TRACK
        var validFrom = LocalDateTime.now().minusDays(30);
        var milestone = LocalDateTime.now().plusDays(60);
        var dto = ParticipantDto.from(row(0, 0), validFrom, milestone);
        assertThat(dto.status()).isEqualTo(ParticipantDto.ParticipantStatus.ON_TRACK);
    }

    @Test
    void onTrack_atBoundary() {
        // 90 day program, 45 days elapsed -> 50%
        // allocated=4, threshold = 4 * (45/90) * 0.8 = 1.6
        // consumed=2 >= 1.6 -> ON_TRACK
        var validFrom = LocalDateTime.now().minusDays(45);
        var milestone = LocalDateTime.now().plusDays(45);
        var dto = ParticipantDto.from(row(2, 4), validFrom, milestone);
        assertThat(dto.status()).isEqualTo(ParticipantDto.ParticipantStatus.ON_TRACK);
    }

    @Test
    void onTrack_whenProgramFullyElapsed() {
        // daysElapsed capped at totalDays
        // allocated=4, threshold = 4 * (90/90) * 0.8 = 3.2
        // consumed=4 >= 3.2 -> ON_TRACK
        var validFrom = LocalDateTime.now().minusDays(100);
        var milestone = LocalDateTime.now().minusDays(10);
        var dto = ParticipantDto.from(row(4, 4), validFrom, milestone);
        assertThat(dto.status()).isEqualTo(ParticipantDto.ParticipantStatus.ON_TRACK);
    }
}
