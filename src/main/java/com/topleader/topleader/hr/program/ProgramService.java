package com.topleader.topleader.hr.program;

import com.topleader.topleader.coach.Coach;
import com.topleader.topleader.coach.CoachRepository;
import com.topleader.topleader.common.ai.AiClient;
import com.topleader.topleader.common.exception.ApiValidationException;
import com.topleader.topleader.common.exception.NotFoundException;
import com.topleader.topleader.hr.program.participant.ProgramParticipant;
import com.topleader.topleader.hr.program.participant.ProgramParticipantRepository;
import com.topleader.topleader.session.coaching_package.CoachingPackage;
import com.topleader.topleader.session.coaching_package.CoachingPackageService;
import com.topleader.topleader.session.coaching_package.dto.CreateCoachingPackageRequest;
import com.topleader.topleader.session.user_allocation.UserAllocation;
import com.topleader.topleader.session.user_allocation.UserAllocationRepository;
import com.topleader.topleader.user.User;
import com.topleader.topleader.user.UserDetailService;
import com.topleader.topleader.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.topleader.topleader.common.exception.ErrorCodeConstants.*;
import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
public class ProgramService {

    static final double AT_RISK_GRACE_BUFFER = 0.8;

    private final ProgramRepository programRepository;
    private final ProgramParticipantRepository participantRepository;
    private final UserAllocationRepository allocationRepository;
    private final CoachingPackageService coachingPackageService;
    private final UserDetailService userDetailService;
    private final CoachRepository coachRepository;
    private final UserRepository userRepository;
    private final AiClient aiClient;
    private final ProgramOptionRepository programOptionRepository;

    public List<ProgramOption> findAllOptions() {
        return programOptionRepository.findAll();
    }

    public List<ProgramRepository.ProgramSummaryRow> listPrograms(String username) {
        var companyId = resolveCompanyId(username);
        return programRepository.findSummariesByCompanyId(companyId);
    }

    @Transactional
    public Program saveDraft(String username, ProgramController.SaveProgramRequest request) {
        var companyId = resolveCompanyId(username);
        var now = LocalDateTime.now();

        var program = Optional.ofNullable(request.id())
                .map(id -> {
                    verifyProgramOwnership(id, companyId);
                    return programRepository.findById(id)
                            .filter(p -> Program.Status.DRAFT.equals(p.getStatus()))
                            .orElseThrow(NotFoundException::new);
                })
                .orElseGet(() -> {
                    var validFrom = request.startDate();
                    var validTo = validFrom != null && request.durationDays() > 0
                            ? validFrom.plusDays(request.durationDays())
                            : validFrom;
                    var pkg = coachingPackageService.createPackage(companyId,
                            new CreateCoachingPackageRequest(CoachingPackage.PoolType.CORE, 0, validFrom, validTo, null),
                            username);
                    return new Program()
                            .setCoachingPackageId(pkg.id())
                            .setStatus(Program.Status.DRAFT)
                            .setCreatedAt(now)
                            .setCreatedBy(username);
                });

        program.setName(request.name())
                .setGoal(request.goal())
                .setTargetGroup(request.targetGroup())
                .setDurationDays(request.durationDays())
                .setFocusAreas(request.focusAreas())
                .setMilestoneDate(request.milestoneDate())
                .setSessionsPerParticipant(request.sessionsPerParticipant())
                .setRecommendedCadence(request.recommendedCadence())
                .setCoachAssignmentModel(Optional.ofNullable(request.coachAssignmentModel()).orElse(Program.CoachAssignmentModel.PARTICIPANT_CHOOSES))
                .setShortlistedCoaches(request.shortlistedCoaches())
                .setMicroActionsEnabled(request.microActionsEnabled())
                .setEnabledOptions(request.enabledOptions())
                .setUpdatedAt(now)
                .setUpdatedBy(username);

        var saved = programRepository.save(program);
        syncParticipants(saved, request.participants(), companyId, username, now);
        return saved;
    }

    public ProgramController.ProgramDetailDto getProgramDetail(String username, Long programId) {
        var companyId = resolveCompanyId(username);
        var program = programRepository.findByIdAndCompanyId(programId, companyId)
                .orElseThrow(NotFoundException::new);
        var participants = programRepository.findParticipants(programId).stream()
                .map(row -> ProgramController.ParticipantDto.from(row, program.validFrom(), program.milestoneDate()))
                .toList();
        return ProgramController.ProgramDetailDto.from(program, ProgramController.ProgramStatsDto.from(participants), participants);
    }

    public List<ProgramRepository.CompanyUserRow> getCompanyUsers(String username, Long programId) {
        var companyId = resolveCompanyId(username);
        if (programId != null) {
            verifyProgramOwnership(programId, companyId);
        }
        return programRepository.findCompanyUsersWithParticipation(programId, companyId);
    }

    public List<ProgramController.RecommendedCoachDto> recommendCoaches(ProgramController.RecommendCoachesRequest request) {
        var coaches = new ArrayList<Coach>();
        coachRepository.findAll().forEach(coaches::add);

        var coachUsernames = coaches.stream().map(Coach::getUsername).toList();
        if (coachUsernames.isEmpty()) {
            return List.of();
        }
        var usersMap = userRepository.findAllByUsernameIn(coachUsernames).stream()
                .collect(toMap(User::getUsername, u -> u));

        var publicCoaches = coaches.stream()
                .filter(Coach::isPublicProfile)
                .toList();

        var profiles = publicCoaches.stream()
                .map(c -> {
                    var u = usersMap.get(c.getUsername());
                    return new AiClient.CoachProfile(
                            c.getUsername(),
                            u != null ? u.getFirstName() : "",
                            u != null ? u.getLastName() : "",
                            c.getBio(),
                            Optional.ofNullable(c.getPrimaryRoles()).map(Object::toString).orElse(""),
                            Optional.ofNullable(c.getFields()).map(Object::toString).orElse(""),
                            Optional.ofNullable(c.getTopics()).map(Object::toString).orElse(""),
                            c.getPriority()
                    );
                })
                .toList();

        var recommendationsMap = aiClient.recommendCoaches(
                request.goal(),
                request.focusAreas().stream().toList(),
                Optional.ofNullable(request.targetGroup()).orElse(""),
                profiles
        ).stream().collect(toMap(AiClient.CoachRecommendation::username, AiClient.CoachRecommendation::reason));

        return publicCoaches.stream()
                .map(c -> {
                    var u = usersMap.get(c.getUsername());
                    return new ProgramController.RecommendedCoachDto(
                            c.getUsername(),
                            u != null ? u.getFirstName() : "",
                            u != null ? u.getLastName() : "",
                            recommendationsMap.get(c.getUsername())
                    );
                })
                .sorted(Comparator.comparing((ProgramController.RecommendedCoachDto d) -> d.reason() == null ? 1 : 0)
                        .thenComparing(ProgramController.RecommendedCoachDto::lastName))
                .toList();
    }

    @Transactional
    public Program launchProgram(String username, Long programId) {
        var companyId = resolveCompanyId(username);
        var now = LocalDateTime.now();

        verifyProgramOwnership(programId, companyId);
        var program = programRepository.findById(programId)
                .filter(p -> Program.Status.DRAFT.equals(p.getStatus()))
                .orElseThrow(NotFoundException::new);

        validateForLaunch(program, programId);

        program.setStatus(Program.Status.CREATED)
                .setUpdatedAt(now)
                .setUpdatedBy(username);

        return programRepository.save(program);
    }

    private void syncParticipants(Program program, Set<String> requestedUsernames, Long companyId, String username, LocalDateTime now) {
        var programId = program.getId();
        var packageId = program.getCoachingPackageId();
        var existing = participantRepository.findByProgramId(programId);
        var existingUsernames = existing.stream().map(ProgramParticipant::getUsername).collect(Collectors.toSet());

        existing.stream()
                .filter(p -> !requestedUsernames.contains(p.getUsername()))
                .forEach(p -> {
                    participantRepository.deleteByProgramIdAndUsername(programId, p.getUsername());
                    allocationRepository.findByPackageIdAndUsername(packageId, p.getUsername())
                            .ifPresent(allocationRepository::delete);
                });

        requestedUsernames.stream()
                .filter(u -> !existingUsernames.contains(u))
                .forEach(participantUsername -> {
                    participantRepository.save(new ProgramParticipant()
                            .setProgramId(programId)
                            .setUsername(participantUsername)
                            .setCreatedAt(now)
                            .setCreatedBy(username));
                    if (allocationRepository.findByPackageIdAndUsername(packageId, participantUsername).isEmpty()) {
                        allocationRepository.save(new UserAllocation()
                                .setCompanyId(companyId)
                                .setPackageId(packageId)
                                .setUsername(participantUsername)
                                .setAllocatedUnits(0)
                                .setConsumedUnits(0)
                                .setCreatedAt(now)
                                .setCreatedBy(username));
                    }
                });
    }

    private void validateForLaunch(Program program, Long programId) {
        if (program.getGoal() == null || program.getGoal().isBlank()) {
            throw new ApiValidationException(PROGRAM_GOAL_REQUIRED, "goal", "", "Program goal is required for launch");
        }
        if (program.getCoachAssignmentModel() == null) {
            throw new ApiValidationException(COACH_MODEL_REQUIRED, "coachAssignmentModel", "", "Coach assignment model is required for launch");
        }
        if (participantRepository.findByProgramId(programId).isEmpty()) {
            throw new ApiValidationException(PARTICIPANTS_REQUIRED, "participants", "", "At least one participant is required for launch");
        }
    }

    private void verifyProgramOwnership(Long programId, Long companyId) {
        programRepository.findByIdAndCompanyId(programId, companyId)
                .orElseThrow(NotFoundException::new);
    }

    Long resolveCompanyId(String username) {
        var user = userDetailService.getUser(username).orElseThrow(NotFoundException::new);
        return Optional.ofNullable(user.getCompanyId())
                .orElseThrow(() -> new ApiValidationException(NOT_PART_OF_COMPANY, "user", username, "User is not part of any company"));
    }
}
