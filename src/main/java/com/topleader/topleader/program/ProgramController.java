package com.topleader.topleader.program;

import com.topleader.topleader.program.dto.*;
import com.topleader.topleader.program.enrollment.ProgramEnrollmentEmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/latest/hr/programs")
@RequiredArgsConstructor
@Secured({"HR", "ADMIN"})
public class ProgramController {

    private final ProgramService programService;
    private final ProgramEnrollmentEmailService enrollmentEmailService;

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
                .map(r -> new ProgramUserDto(r.username(), r.firstName(), r.lastName(), r.email(), r.added(), r.activeProgramName()))
                .toList();
    }

    @PostMapping("/recommend-coaches")
    public List<RecommendedCoachDto> recommendCoaches(@Valid @RequestBody RecommendCoachesRequest request) {
        return programService.recommendCoaches(request);
    }

    @PostMapping("/{programId}/launch")
    public ProgramDraftDto launchProgram(@AuthenticationPrincipal UserDetails user, @PathVariable Long programId) {
        var program = programService.launchProgram(user.getUsername(), programId);
        enrollmentEmailService.scheduleEnrollmentEmails(program);
        return toDraftDto(program);
    }

    @PostMapping("/{programId}/participants/{username}/resend-enrollment")
    public void resendEnrollmentEmail(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long programId,
            @PathVariable String username
    ) {
        var companyId = programService.resolveCompanyId(user.getUsername());
        enrollmentEmailService.resendEnrollmentEmail(programId, username, companyId);
    }

    @GetMapping("/coach-categories")
    public List<CoachCategoryDto> getCoachCategories() {
        return programService.findAllExpertiseCategories().stream()
                .map(c -> new CoachCategoryDto(c.getKey(), c.getName(), c.getCoachFields()))
                .toList();
    }

    @PostMapping("/coach-preview")
    public CoachPreviewResponse getCoachPreview(@Valid @RequestBody CoachMatchRequest request) {
        return programService.findMatchingExperts(request);
    }

    @GetMapping("/focus-area-mappings")
    public Map<String, String> getFocusAreaMappings() {
        return programService.getFocusAreaCategoryMappings();
    }

    private ProgramDraftDto toDraftDto(Program program) {
        return new ProgramDraftDto(
                program.getId(),
                program.getName(),
                program.getGoal(),
                program.getTargetGroup(),
                program.getStatus().name(),
                program.getDurationDays(),
                program.getCycleLengthDays(),
                computeCheckpoints(program.getDurationDays(), program.getCycleLengthDays()),
                program.getMilestoneDate(),
                program.getFocusAreas(),
                program.getSessionsPerParticipant(),
                program.getRecommendedCadence(),
                program.getCoachAssignmentModel(),
                program.getShortlistedCoaches(),
                program.isMicroActionsEnabled(),
                program.getEnabledOptions(),
                program.getCoachLanguages(),
                program.getCoachCategories()
        );
    }

    static List<CheckpointDto> computeCheckpoints(Integer durationDays, Integer cycleLengthDays) {
        if (durationDays == null || durationDays <= 0) {
            return List.of();
        }
        var cycle = Optional.ofNullable(cycleLengthDays).filter(c -> c > 0).orElse(durationDays);
        var mid = cycle / 2;
        var checkpoints = new ArrayList<CheckpointDto>();
        checkpoints.add(new CheckpointDto("Enrollment", 0));
        for (int day = 0; day < durationDays; day += cycle) {
            if (mid > 0 && day + mid < durationDays) {
                checkpoints.add(new CheckpointDto("Mid-cycle", day + mid));
            }
            if (day + cycle < durationDays) {
                checkpoints.add(new CheckpointDto("Cycle review", day + cycle));
            }
        }
        checkpoints.add(new CheckpointDto("Final review", durationDays));
        return List.copyOf(checkpoints);
    }
}
