package com.topleader.topleader.program;

import com.topleader.topleader.coach.Coach;
import com.topleader.topleader.coach.CoachRepository;
import com.topleader.topleader.common.ai.AiClient;
import com.topleader.topleader.common.exception.ApiValidationException;
import com.topleader.topleader.common.exception.NotFoundException;
import com.topleader.topleader.program.category.ExpertiseCategory;
import com.topleader.topleader.program.category.ExpertiseCategoryRepository;
import com.topleader.topleader.program.category.FocusAreaCategoryMapping;
import com.topleader.topleader.program.category.FocusAreaCategoryMappingRepository;
import com.topleader.topleader.program.dto.*;
import com.topleader.topleader.program.participant.ProgramParticipant;
import com.topleader.topleader.program.participant.ProgramParticipantRepository;
import com.topleader.topleader.program.ranking.CoachRankingService;
import com.topleader.topleader.session.coaching_package.CoachingPackage;
import com.topleader.topleader.session.coaching_package.CoachingPackageService;
import com.topleader.topleader.session.coaching_package.dto.CreateCoachingPackageRequest;
import com.topleader.topleader.session.user_allocation.UserAllocation;
import com.topleader.topleader.session.user_allocation.UserAllocationRepository;
import com.topleader.topleader.user.User;
import com.topleader.topleader.user.UserDetailService;
import com.topleader.topleader.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.topleader.topleader.common.exception.ErrorCodeConstants.*;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

@Service
@RequiredArgsConstructor
public class ProgramService {

    public static final double AT_RISK_GRACE_BUFFER = 0.8;

    @Value("${topleader.coach-pool.min-size:8}")
    private int poolMinSize;

    @Value("${topleader.coach-pool.max-size:15}")
    private int poolMaxSize;

    private final ProgramRepository programRepository;
    private final ProgramParticipantRepository participantRepository;
    private final UserAllocationRepository allocationRepository;
    private final CoachingPackageService coachingPackageService;
    private final UserDetailService userDetailService;
    private final CoachRepository coachRepository;
    private final UserRepository userRepository;
    private final AiClient aiClient;
    private final ProgramOptionRepository programOptionRepository;
    private final ExpertiseCategoryRepository expertiseCategoryRepository;
    private final FocusAreaCategoryMappingRepository focusAreaCategoryMappingRepository;
    private final CoachRankingService coachRankingService;

    public List<ProgramOption> findAllOptions() {
        return programOptionRepository.findAll();
    }

    public List<ProgramRepository.ProgramSummaryRow> listPrograms(String username) {
        var companyId = resolveCompanyId(username);
        return programRepository.findSummariesByCompanyId(companyId);
    }

    @Transactional
    public Program saveDraft(String username, SaveProgramRequest request) {
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
                .setCycleLengthDays(request.cycleLengthDays())
                .setFocusAreas(request.focusAreas())
                .setMilestoneDate(request.milestoneDate())
                .setSessionsPerParticipant(request.sessionsPerParticipant())
                .setRecommendedCadence(request.recommendedCadence())
                .setCoachAssignmentModel(Optional.ofNullable(request.coachAssignmentModel()).orElse(Program.CoachAssignmentModel.PARTICIPANT_CHOOSES))
                .setShortlistedCoaches(request.shortlistedCoaches())
                .setMicroActionsEnabled(request.microActionsEnabled())
                .setEnabledOptions(request.enabledOptions())
                .setCoachLanguages(request.coachLanguages())
                .setCoachCategories(request.coachCategories())
                .setUpdatedAt(now)
                .setUpdatedBy(username);

        var saved = programRepository.save(program);
        syncParticipants(saved, request.participants(), companyId, username, now);
        return saved;
    }

    public ProgramDetailDto getProgramDetail(String username, Long programId) {
        var companyId = resolveCompanyId(username);
        var program = programRepository.findByIdAndCompanyId(programId, companyId)
                .orElseThrow(NotFoundException::new);
        var participants = programRepository.findParticipants(programId).stream()
                .map(row -> ParticipantDto.from(row, program.validFrom(), program.milestoneDate()))
                .toList();
        return ProgramDetailDto.from(program, ProgramStatsDto.from(participants), participants);
    }

    public List<ProgramRepository.CompanyUserRow> getCompanyUsers(String username, Long programId) {
        var companyId = resolveCompanyId(username);
        if (programId != null) {
            verifyProgramOwnership(programId, companyId);
        }
        return programRepository.findCompanyUsersWithParticipation(programId, companyId);
    }

    public List<RecommendedCoachDto> recommendCoaches(RecommendCoachesRequest request) {
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
                            Optional.ofNullable(c.getPrimaryRoles()).map(Object::toString).orElse(StringUtils.EMPTY),
                            Optional.ofNullable(c.getFields()).map(Object::toString).orElse(StringUtils.EMPTY),
                            Optional.ofNullable(c.getTopics()).map(Object::toString).orElse(StringUtils.EMPTY),
                            c.getPriority()
                    );
                })
                .toList();

        var recommendationsMap = aiClient.recommendCoaches(
                request.goal(),
                request.focusAreas().stream().toList(),
                Optional.ofNullable(request.targetGroup()).orElse(StringUtils.EMPTY),
                profiles
        ).stream().collect(toMap(AiClient.CoachRecommendation::username, AiClient.CoachRecommendation::reason));

        return publicCoaches.stream()
                .map(c -> {
                    var u = usersMap.get(c.getUsername());
                    return new RecommendedCoachDto(
                            c.getUsername(),
                            u != null ? u.getFirstName() : "",
                            u != null ? u.getLastName() : "",
                            recommendationsMap.get(c.getUsername())
                    );
                })
                .sorted(Comparator.comparing((RecommendedCoachDto d) -> d.reason() == null ? 1 : 0)
                        .thenComparing(RecommendedCoachDto::lastName))
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

        allocationRepository.findByPackageId(program.getCoachingPackageId())
                .forEach(a -> allocationRepository.save(a.setAllocatedUnits(program.getSessionsPerParticipant())));

        return programRepository.save(program);
    }

    public List<ExpertiseCategory> findAllExpertiseCategories() {
        return expertiseCategoryRepository.findAll();
    }

    public Map<String, String> getFocusAreaCategoryMappings() {
        return focusAreaCategoryMappingRepository.findAll().stream()
                .collect(toMap(FocusAreaCategoryMapping::getFocusAreaKey, FocusAreaCategoryMapping::getCategoryKey));
    }

    // Target pool size: clamp(minSize, participants × 2, maxSize)
    int targetPoolSize(int participantCount) {
        return Math.max(poolMinSize, Math.min(participantCount * 2, poolMaxSize));
    }

    // Orchestrates coach preview: hard filter → find extras if needed → AI rank → build response
    public CoachPreviewResponse findMatchingExperts(CoachMatchRequest request) {
        var target = targetPoolSize(request.participantCount());
        var exact = matchCoaches(request.coachLanguages(), request.coachCategories());
        var extras = findLanguageOnlyExtras(exact, request.coachLanguages(), target);
        var allCandidates = Stream.concat(exact.stream(), extras.stream()).toList();

        if (allCandidates.isEmpty()) {
            return new CoachPreviewResponse(0, 0, 0, "0 experts match", List.of());
        }

        var usersMap = resolveUsers(allCandidates);
        var aiResult = coachRankingService.rank(allCandidates, usersMap, request, target);
        var pool = coachRankingService.buildPool(exact, extras, aiResult, usersMap, target);

        return coachRankingService.toResponse(pool, aiResult.rankOrder());
    }

    // Coaches matching language only (no category) — candidates for AI to recommend if exact < target
    private List<Coach> findLanguageOnlyExtras(List<Coach> exact, Set<String> languages, int target) {
        if (exact.size() >= target) {
            return List.of();
        }
        var exactUsernames = exact.stream().map(Coach::getUsername).collect(toSet());
        return getAllPublicCoaches().stream()
                .filter(c -> !exactUsernames.contains(c.getUsername()))
                .filter(c -> matchesLanguage(c, languages))
                .toList();
    }

    // Resolve User entities for coach usernames (needed for firstName/lastName)
    private Map<String, User> resolveUsers(List<Coach> coaches) {
        return userRepository.findAllByUsernameIn(
                coaches.stream().map(Coach::getUsername).toList()
        ).stream().collect(toMap(User::getUsername, u -> u));
    }

    // Hard filter: language (show-stopper) + expertise category. Both AND.
    // Empty language = no restriction. AI cannot override these.
    private List<Coach> matchCoaches(Set<String> languages, Set<String> categoryKeys) {
        if (categoryKeys.isEmpty()) {
            return List.of();
        }

        // Resolve category keys → union of all coach.fields values they map to
        var resolvedFields = expertiseCategoryRepository.findAllById(categoryKeys).stream()
                .flatMap(c -> c.getCoachFields().stream())
                .collect(toSet());

        if (resolvedFields.isEmpty()) {
            return List.of();
        }

        return getAllPublicCoaches().stream()
                .filter(c -> {
                    var coachFields = c.getFieldsList();
                    return coachFields != null && coachFields.stream().anyMatch(resolvedFields::contains);
                })
                .filter(c -> matchesLanguage(c, languages))
                .toList();
    }

    private List<Coach> getAllPublicCoaches() {
        var coaches = new ArrayList<Coach>();
        coachRepository.findAll().forEach(coaches::add);
        return coaches.stream().filter(Coach::isPublicProfile).toList();
    }

    // Language check — show-stopper when set. Empty = no restriction.
    // Coach can have multiple languages — at least one must match.
    private boolean matchesLanguage(Coach c, Set<String> languages) {
        if (languages.isEmpty()) {
            return true;
        }
        var coachLangs = c.getLanguagesList();
        return coachLangs != null && coachLangs.stream().anyMatch(languages::contains);
    }

    private void syncParticipants(Program program, List<ParticipantAssignment> assignments, Long companyId, String username, LocalDateTime now) {
        var programId = program.getId();
        var packageId = program.getCoachingPackageId();
        var existing = participantRepository.findByProgramId(programId);
        var existingByUsername = existing.stream().collect(toMap(ProgramParticipant::getUsername, p -> p));
        var assignmentMap = assignments.stream().collect(toMap(ParticipantAssignment::username, a -> a));
        var requestedUsernames = assignmentMap.keySet();

        existing.stream()
                .filter(p -> !requestedUsernames.contains(p.getUsername()))
                .forEach(p -> {
                    participantRepository.deleteByProgramIdAndUsername(programId, p.getUsername());
                    allocationRepository.findByPackageIdAndUsername(packageId, p.getUsername())
                            .ifPresent(allocationRepository::delete);
                });

        // Update manager for existing participants
        existing.stream()
                .filter(p -> requestedUsernames.contains(p.getUsername()))
                .forEach(p -> {
                    var assignment = assignmentMap.get(p.getUsername());
                    if (!Objects.equals(p.getManagerUsername(), assignment.managerUsername())) {
                        participantRepository.save(p.setManagerUsername(assignment.managerUsername()));
                    }
                });

        var newUsernames = requestedUsernames.stream()
                .filter(u -> !existingByUsername.containsKey(u))
                .collect(Collectors.toSet());
        if (!newUsernames.isEmpty()) {
            var existingDbUsernames = userRepository.findAllByUsernameIn(newUsernames).stream()
                    .map(User::getUsername)
                    .collect(Collectors.toSet());
            var invalidUsernames = newUsernames.stream()
                    .filter(u -> !existingDbUsernames.contains(u))
                    .collect(Collectors.toSet());
            if (!invalidUsernames.isEmpty()) {
                throw new ApiValidationException(PARTICIPANTS_NOT_FOUND, "participants", invalidUsernames.toString(), "Some participants do not exist: " + invalidUsernames);
            }

            var alreadyInProgram = participantRepository.findUsernamesInActivePrograms(newUsernames, programId);
            if (!alreadyInProgram.isEmpty()) {
                throw new ApiValidationException(PARTICIPANT_ALREADY_IN_PROGRAM, "participants", alreadyInProgram.toString(), "Participants already in an active program: " + alreadyInProgram);
            }
        }

        newUsernames.forEach(participantUsername -> {
                    var assignment = assignmentMap.get(participantUsername);
                    participantRepository.save(new ProgramParticipant()
                            .setProgramId(programId)
                            .setUsername(participantUsername)
                            .setManagerUsername(assignment.managerUsername())
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
