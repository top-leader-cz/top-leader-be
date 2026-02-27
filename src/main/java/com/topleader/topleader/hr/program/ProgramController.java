package com.topleader.topleader.hr.program;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/latest/hr/programs")
@RequiredArgsConstructor
@Secured({"HR", "ADMIN"})
public class ProgramController {

    private final ProgramService programService;

    @GetMapping("/options")
    public List<ProgramOptionDto> getOptions() {
        return programService.findAllOptions().stream()
                .map(o -> new ProgramOptionDto(o.getKey(), o.getCategory(), o.isAlwaysOn()))
                .toList();
    }

    @GetMapping
    public List<ProgramSummaryDto> listPrograms(@AuthenticationPrincipal UserDetails user) {
        return programService.listPrograms(user.getUsername()).stream()
                .map(ProgramSummaryDto::from)
                .toList();
    }

    @PostMapping
    public ProgramDraftDto saveDraft(@AuthenticationPrincipal UserDetails user, @Valid @RequestBody SaveProgramRequest request) {
        return toDraftDto(programService.saveDraft(user.getUsername(), request));
    }

    @GetMapping("/{programId}")
    public ProgramDetailDto getProgram(@AuthenticationPrincipal UserDetails user, @PathVariable Long programId) {
        return programService.getProgramDetail(user.getUsername(), programId);
    }

    @GetMapping("/users")
    public List<ProgramUserDto> getCompanyUsers(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam(required = false) Long programId
    ) {
        return programService.getCompanyUsers(user.getUsername(), programId).stream()
                .map(r -> new ProgramUserDto(r.username(), r.firstName(), r.lastName(), r.email(), r.added()))
                .toList();
    }

    @PostMapping("/recommend-coaches")
    public List<RecommendedCoachDto> recommendCoaches(@Valid @RequestBody RecommendCoachesRequest request) {
        return programService.recommendCoaches(request);
    }

    @PostMapping("/{programId}/launch")
    public ProgramDraftDto launchProgram(@AuthenticationPrincipal UserDetails user, @PathVariable Long programId) {
        return toDraftDto(programService.launchProgram(user.getUsername(), programId));
    }

    private ProgramDraftDto toDraftDto(Program program) {
        return new ProgramDraftDto(
                program.getId(),
                program.getName(),
                program.getGoal(),
                program.getTargetGroup(),
                program.getStatus().name(),
                program.getDurationDays(),
                program.getMilestoneDate(),
                program.getFocusAreas(),
                program.getSessionsPerParticipant(),
                program.getRecommendedCadence(),
                program.getCoachAssignmentModel(),
                program.getShortlistedCoaches(),
                program.isMicroActionsEnabled(),
                program.getEnabledOptions()
        );
    }

    static Long daysUntil(LocalDateTime date) {
        if (date == null) return null;
        return ChronoUnit.DAYS.between(LocalDateTime.now(), date);
    }

    // --- DTOs ---

    public record SaveProgramRequest(
            Long id,
            @NotBlank String name,
            String goal,
            String targetGroup,
            int durationDays,
            LocalDateTime startDate,
            LocalDateTime milestoneDate,
            @NotNull Set<String> focusAreas,
            @NotNull Set<String> participants,
            int sessionsPerParticipant,
            String recommendedCadence,
            Program.CoachAssignmentModel coachAssignmentModel,
            @NotNull Set<String> shortlistedCoaches,
            boolean microActionsEnabled,
            @NotNull Set<String> enabledOptions
    ) {}

    public record ProgramDraftDto(
            Long id,
            String name,
            String goal,
            String targetGroup,
            String status,
            Integer durationDays,
            LocalDateTime milestoneDate,
            Set<String> focusAreas,
            Integer sessionsPerParticipant,
            String recommendedCadence,
            Program.CoachAssignmentModel coachAssignmentModel,
            Set<String> shortlistedCoaches,
            boolean microActionsEnabled,
            Set<String> enabledOptions
    ) {}

    public record ProgramSummaryDto(
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

    public record ProgramDetailDto(
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

    public record ProgramStatsDto(int totalParticipants, int progressPercent) {
        static ProgramStatsDto from(List<ParticipantDto> participants) {
            var total = participants.size();
            var totalConsumed = participants.stream().mapToInt(ParticipantDto::sessionsConsumed).sum();
            var totalAllocated = participants.stream().mapToInt(ParticipantDto::sessionsAllocated).sum();
            var progress = totalAllocated == 0 ? 0 : (int) Math.round(totalConsumed * 100.0 / totalAllocated);
            return new ProgramStatsDto(total, progress);
        }
    }

    public enum ParticipantStatus {
        NOT_STARTED, ON_TRACK, AT_RISK
    }

    public record ParticipantDto(
            String username,
            String firstName,
            String lastName,
            String coachUsername,
            LocalDateTime lastActiveAt,
            int sessionsConsumed,
            int sessionsAllocated,
            ParticipantStatus status
    ) {
        static ParticipantDto from(ProgramRepository.ParticipantRow row, LocalDateTime validFrom, LocalDateTime milestoneDate) {
            return new ParticipantDto(
                    row.username(),
                    row.firstName(),
                    row.lastName(),
                    row.coachUsername(),
                    row.lastLoginAt(),
                    row.consumedUnits(),
                    row.allocatedUnits(),
                    computeStatus(row, validFrom, milestoneDate)
            );
        }

        private static ParticipantStatus computeStatus(ProgramRepository.ParticipantRow row, LocalDateTime validFrom, LocalDateTime milestoneDate) {
            var totalDays = validFrom != null && milestoneDate != null
                    ? ChronoUnit.DAYS.between(validFrom, milestoneDate)
                    : 0L;
            var daysElapsed = totalDays > 0
                    ? Math.min(ChronoUnit.DAYS.between(validFrom, LocalDateTime.now()), totalDays)
                    : 0L;
            if (daysElapsed <= 0) {
                return ParticipantStatus.NOT_STARTED;
            }
            var threshold = row.allocatedUnits() * ((double) daysElapsed / totalDays) * ProgramService.AT_RISK_GRACE_BUFFER;
            return row.consumedUnits() < threshold ? ParticipantStatus.AT_RISK : ParticipantStatus.ON_TRACK;
        }
    }

    public record ProgramUserDto(String username, String firstName, String lastName, String email, boolean added) {}

    public record RecommendCoachesRequest(
            @NotBlank String goal,
            @NotNull Set<String> focusAreas,
            String targetGroup
    ) {}

    public record RecommendedCoachDto(String username, String firstName, String lastName, String reason) {}

    public record ProgramOptionDto(String key, String category, boolean alwaysOn) {}
}
