package com.topleader.topleader.program.dto;

import java.util.List;

public record ProgramStatsDto(int totalParticipants, int progressPercent) {

    public static ProgramStatsDto from(List<ParticipantDto> participants) {
        var total = participants.size();
        var totalConsumed = participants.stream().mapToInt(ParticipantDto::sessionsConsumed).sum();
        var totalAllocated = participants.stream().mapToInt(ParticipantDto::sessionsAllocated).sum();
        var progress = totalAllocated == 0 ? 0 : (int) Math.round(totalConsumed * 100.0 / totalAllocated);
        return new ProgramStatsDto(total, progress);
    }
}
