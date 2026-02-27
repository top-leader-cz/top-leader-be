package com.topleader.topleader.hr.program;

import com.topleader.topleader.common.exception.NotFoundException;
import com.topleader.topleader.user.User;
import com.topleader.topleader.user.UserDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/latest/hr/programs")
@RequiredArgsConstructor
@Secured({"HR", "ADMIN"})
public class ProgramController {

    private final ProgramRepository programRepository;
    private final UserDetailService userDetailService;

    @GetMapping
    public List<ProgramSummaryDto> listPrograms(@AuthenticationPrincipal UserDetails user) {
        var companyId = resolveCompanyId(user.getUsername());
        return programRepository.findSummariesByCompanyId(companyId).stream()
                .map(ProgramSummaryDto::from)
                .toList();
    }

    @GetMapping("/{programId}")
    public ProgramDetailDto getProgram(@AuthenticationPrincipal UserDetails user, @PathVariable Long programId) {
        var companyId = resolveCompanyId(user.getUsername());
        var program = programRepository.findByIdAndCompanyId(programId, companyId)
                .orElseThrow(NotFoundException::new);
        var participants = programRepository.findParticipants(programId).stream()
                .map(ParticipantDto::from)
                .toList();
        return ProgramDetailDto.from(program, ProgramStatsDto.from(participants), participants);
    }

    private Long resolveCompanyId(String username) {
        return userDetailService.getUser(username)
                .map(User::getCompanyId)
                .orElseThrow(NotFoundException::new);
    }

    record ProgramSummaryDto(
            Long id,
            String name,
            String status,
            LocalDateTime validFrom,
            LocalDateTime validTo,
            LocalDateTime milestoneDate,
            Long daysUntilMilestone,
            int totalParticipants,
            int activeParticipants
    ) {
        static ProgramSummaryDto from(ProgramRepository.ProgramSummaryRow row) {
            return new ProgramSummaryDto(
                    row.id(),
                    row.name(),
                    row.status(),
                    row.validFrom(),
                    row.validTo(),
                    row.milestoneDate(),
                    daysUntil(row.milestoneDate()),
                    row.totalParticipants(),
                    row.activeParticipants()
            );
        }
    }

    record ProgramDetailDto(
            Long id,
            String name,
            String status,
            LocalDateTime validFrom,
            LocalDateTime validTo,
            LocalDateTime milestoneDate,
            Long daysUntilMilestone,
            ProgramStatsDto stats,
            List<ParticipantDto> participants
    ) {
        static ProgramDetailDto from(ProgramRepository.ProgramRow program, ProgramStatsDto stats, List<ParticipantDto> participants) {
            return new ProgramDetailDto(
                    program.id(),
                    program.name(),
                    program.status(),
                    program.validFrom(),
                    program.validTo(),
                    program.milestoneDate(),
                    daysUntil(program.milestoneDate()),
                    stats,
                    participants
            );
        }
    }

    record ProgramStatsDto(int totalParticipants, int activeParticipants, int progressPercent) {
        static ProgramStatsDto from(List<ParticipantDto> participants) {
            int total = participants.size();
            int active = (int) participants.stream()
                    .filter(p -> !ProgramParticipant.Status.ON_HOLD.name().equals(p.status()))
                    .count();
            int totalConsumed = participants.stream().mapToInt(ParticipantDto::sessionsConsumed).sum();
            int totalAllocated = participants.stream().mapToInt(ParticipantDto::sessionsAllocated).sum();
            int progress = totalAllocated == 0 ? 0 : (int) Math.round(totalConsumed * 100.0 / totalAllocated);
            return new ProgramStatsDto(total, active, progress);
        }
    }

    record ParticipantDto(
            String username,
            String firstName,
            String lastName,
            String coachUsername,
            LocalDateTime lastActiveAt,
            int sessionsConsumed,
            int sessionsAllocated,
            String status
    ) {
        static ParticipantDto from(ProgramRepository.ParticipantRow row) {
            return new ParticipantDto(
                    row.username(),
                    row.firstName(),
                    row.lastName(),
                    row.coachUsername(),
                    row.lastLoginAt(),
                    row.consumedUnits(),
                    row.allocatedUnits(),
                    row.participantStatus()
            );
        }
    }

    private static Long daysUntil(LocalDateTime date) {
        if (date == null) return null;
        return ChronoUnit.DAYS.between(LocalDateTime.now(), date);
    }
}
