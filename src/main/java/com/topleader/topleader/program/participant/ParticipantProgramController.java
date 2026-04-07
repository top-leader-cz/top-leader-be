package com.topleader.topleader.program.participant;

import com.topleader.topleader.program.Program;
import com.topleader.topleader.program.ProgramRepository;
import com.topleader.topleader.program.recommendation.LearnMoreDto;
import com.topleader.topleader.program.recommendation.ProgramRecommendationService;
import com.topleader.topleader.common.ai.AiClient;
import com.topleader.topleader.common.exception.ApiValidationException;
import static com.topleader.topleader.common.exception.ErrorCodeConstants.*;
import com.topleader.topleader.common.exception.NotFoundException;
import com.topleader.topleader.program.participant.assessment.AssessmentQuestion;
import com.topleader.topleader.program.participant.assessment.AssessmentQuestionRepository;
import com.topleader.topleader.program.participant.assessment.AssessmentResponse;
import com.topleader.topleader.program.participant.assessment.AssessmentResponseRepository;
import com.topleader.topleader.program.participant.practice.WeeklyPractice;
import com.topleader.topleader.program.participant.practice.WeeklyPracticeRepository;
import com.topleader.topleader.session.scheduled_session.ScheduledSession;
import com.topleader.topleader.session.scheduled_session.ScheduledSessionRepository;
import com.topleader.topleader.session.user_allocation.UserAllocation;
import com.topleader.topleader.session.user_allocation.UserAllocationRepository;
import com.topleader.topleader.user.UserRepository;
import com.topleader.topleader.user.util.UserUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.dao.DuplicateKeyException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/api/latest/participant/programs")
@Secured({"USER"})
@RequiredArgsConstructor
public class ParticipantProgramController {

    private static final int DAYS_PER_WEEK = 7;
    private static final int DEFAULT_CYCLE_LENGTH_DAYS = 90;
    private static final int EXPECTED_QUESTIONS_PEROCUS_AREA = 5;
    private static final int FIRST_WEEK = 1;
    private static final Set<ProgramParticipant.Status> ACTIVE_STATUSES = Set.of(
            ProgramParticipant.Status.ACTIVE, ProgramParticipant.Status.AT_RISK);
    private static final Set<ProgramParticipant.Status> ENROLLMENT_STATUSES = Set.of(
            ProgramParticipant.Status.INVITED, ProgramParticipant.Status.ENROLLING);
    private static final Map<String, String> FALLBACK_PRACTICE = Map.of(
            "en", "Reflect on your focus area and set one small goal for this week",
            "cs", "Zamyslete se nad svou oblastí zaměření a stanovte si jeden malý cíl na tento týden",
            "de", "Denken Sie über Ihren Schwerpunkt nach und setzen Sie sich ein kleines Ziel für diese Woche",
            "fr", "Réfléchissez à votre domaine d'intérêt et fixez-vous un petit objectif pour cette semaine"
    );

    private final ProgramParticipantRepository participantRepository;
    private final ProgramRepository programRepository;
    private final AssessmentQuestionRepository assessmentQuestionRepository;
    private final AssessmentResponseRepository assessmentResponseRepository;
    private final WeeklyPracticeRepository weeklyPracticeRepository;
    private final ScheduledSessionRepository scheduledSessionRepository;
    private final UserAllocationRepository userAllocationRepository;
    private final UserRepository userRepository;
    private final AiClient aiClient;
    private final ProgramRecommendationService recommendationService;

    @GetMapping("/status")
    public ProgramStatusDto getStatus(@AuthenticationPrincipal UserDetails user) {
        return participantRepository.findActiveByUsername(user.getUsername())
                .map(p -> {
                    var program = programRepository.findById(p.getProgramId())
                            .orElseThrow(NotFoundException::new);
                    return ProgramStatusDto.of(p, program);
                })
                .orElse(ProgramStatusDto.none());
    }

    @GetMapping("/{programId}/dashboard")
    public DashboardDto getDashboard(
            @PathVariable Long programId,
            @AuthenticationPrincipal UserDetails user) {
        var participant = participantRepository.findByProgramIdAndUsername(programId, user.getUsername())
                .orElseThrow(NotFoundException::new);
        var program = programRepository.findById(programId)
                .orElseThrow(NotFoundException::new);
        var practice = ensureCurrentWeekPractice(participant, program);
        var nextSession = scheduledSessionRepository
                .findAllByUsernameAndTimeIsAfterAndStatusUpcoming(user.getUsername(), LocalDateTime.now())
                .stream().min((a, b) -> a.getTime().compareTo(b.getTime())).orElse(null);
        var allocation = userAllocationRepository
                .findByPackageIdAndUsername(program.getCoachingPackageId(), user.getUsername())
                .orElse(null);
        var midCycleStatus = computeAssessmentStatus(participant, program, AssessmentResponse.Type.MID);
        var finalStatus = computeAssessmentStatus(participant, program, AssessmentResponse.Type.FINAL);
        var language = resolveLanguage(user.getUsername());
        var learnMore = recommendationService.loadForParticipant(participant, language);
        return DashboardDto.of(participant, program, practice, nextSession, allocation, midCycleStatus, finalStatus, learnMore);
    }

    private AssessmentStatus computeAssessmentStatus(
            ProgramParticipant participant, Program program, AssessmentResponse.Type type) {
        if (participant.getEnrolledAt() == null) {
            return AssessmentStatus.NOT_DUE;
        }
        var existing = assessmentResponseRepository.findByParticipantIdAndTypeAndCycle(
                participant.getId(), type, participant.getCurrentCycle());
        if (existing.isPresent()) {
            return AssessmentStatus.COMPLETED;
        }
        var cycleLengthDays = effectiveCycleLength(program);
        var dueDate = switch (type) {
            case MID -> participant.getEnrolledAt()
                    .plusDays((long) (participant.getCurrentCycle() - 1) * cycleLengthDays)
                    .plusDays(cycleLengthDays / 2);
            case FINAL -> participant.getEnrolledAt()
                    .plusDays((long) participant.getCurrentCycle() * cycleLengthDays);
            default -> throw new IllegalArgumentException("Unsupported assessment type: " + type);
        };
        return LocalDateTime.now().isAfter(dueDate) ? AssessmentStatus.DUE : AssessmentStatus.NOT_DUE;
    }

    @GetMapping("/{programId}/focus-areas")
    public EnrollmentInfoDto getEnrollmentInfo(
            @PathVariable Long programId,
            @AuthenticationPrincipal UserDetails user) {
        var participant = participantRepository.findByProgramIdAndUsername(programId, user.getUsername())
                .orElseThrow(NotFoundException::new);
        var program = programRepository.findById(programId)
                .orElseThrow(NotFoundException::new);
        return new EnrollmentInfoDto(
                program.getGoal(),
                program.getFocusAreas(),
                participant.getFocusArea(),
                participant.getPersonalGoal()
        );
    }

    @PostMapping("/{programId}/enroll")
    public void enroll(
            @PathVariable Long programId,
            @RequestBody @Valid EnrollRequest request,
            @AuthenticationPrincipal UserDetails user) {
        var participant = participantRepository.findByProgramIdAndUsername(programId, user.getUsername())
                .orElseThrow(NotFoundException::new);
        requireStatus(participant, ENROLLMENT_STATUSES, PARTICIPANT_ENROLL_INVALID_STATUS);
        var program = programRepository.findById(programId).orElseThrow(NotFoundException::new);
        requireValidFocusArea(program, request.focusArea());
        participant.setFocusArea(request.focusArea())
                .setPersonalGoal(request.personalGoal())
                .setStatus(ProgramParticipant.Status.ENROLLING);
        if (participant.getEnrolledAt() == null) {
            participant.setEnrolledAt(LocalDateTime.now());
        }
        participantRepository.save(participant);
    }

    @GetMapping("/{programId}/assessment/{type}")
    public AssessmentQuestionsDto getAssessmentQuestions(
            @PathVariable Long programId,
            @PathVariable String type,
            @AuthenticationPrincipal UserDetails user) {
        var assessmentType = AssessmentResponse.Type.valueOf(type.toUpperCase());
        var participant = participantRepository.findByProgramIdAndUsername(programId, user.getUsername())
                .orElseThrow(NotFoundException::new);
        if (participant.getFocusArea() == null) {
            throw new NotFoundException();
        }
        var questions = findAndValidateQuestions(participant.getFocusArea());
        var existing = assessmentResponseRepository.findByParticipantIdAndTypeAndCycle(
                participant.getId(), assessmentType, participant.getCurrentCycle());
        return AssessmentQuestionsDto.of(participant.getFocusArea(), questions, existing.orElse(null));
    }

    @PostMapping("/{programId}/assessment/baseline")
    public List<String> submitBaseline(
            @PathVariable Long programId,
            @RequestBody @Valid AssessmentAnswersRequest request,
            @AuthenticationPrincipal UserDetails user) {
        var participant = participantRepository.findByProgramIdAndUsername(programId, user.getUsername())
                .orElseThrow(NotFoundException::new);
        requireStatus(participant, Set.of(ProgramParticipant.Status.ENROLLING), PARTICIPANT_BASELINE_INVALID_STATUS);
        var program = programRepository.findById(programId).orElseThrow(NotFoundException::new);
        upsertAssessmentResponse(participant, AssessmentResponse.Type.BASELINE, request);
        participant.setStatus(ProgramParticipant.Status.ACTIVE);
        participantRepository.save(participant);

        var language = resolveLanguage(user.getUsername());
        var fallback = List.of(resolveFallbackPractice(user.getUsername()));
        List<String> suggestions;
        try {
            suggestions = Optional.ofNullable(aiClient.generateWeeklyPractices(
                    participant.getFocusArea(), participant.getPersonalGoal(), program.getGoal(), language))
                    .filter(s -> !s.isEmpty())
                    .orElse(fallback);
        } catch (Exception e) {
            suggestions = fallback;
        }
        weeklyPracticeRepository.save(new WeeklyPractice()
                .setParticipantId(participant.getId())
                .setCycle(participant.getCurrentCycle())
                .setWeekNumber(FIRST_WEEK)
                .setText(suggestions.getFirst())
                .setSource(WeeklyPractice.Source.AI)
                .setCreatedAt(LocalDateTime.now()));
        recommendationService.generateAsync(participant, language);
        return suggestions;
    }

    @PutMapping("/{programId}/practice")
    public void updatePractice(
            @PathVariable Long programId,
            @RequestBody @Valid PracticeUpdateRequest request,
            @AuthenticationPrincipal UserDetails user) {
        var participant = participantRepository.findByProgramIdAndUsername(programId, user.getUsername())
                .orElseThrow(NotFoundException::new);
        var practice = weeklyPracticeRepository
                .findByParticipantIdAndCycleAndWeekNumber(participant.getId(), participant.getCurrentCycle(), request.weekNumber())
                .orElseThrow(NotFoundException::new);
        practice.setText(request.text())
                .setSource(WeeklyPractice.Source.EDITED);
        weeklyPracticeRepository.save(practice);
    }

    @PostMapping("/{programId}/practice/suggest")
    public List<String> suggestPractice(
            @PathVariable Long programId,
            @AuthenticationPrincipal UserDetails user) {
        var participant = participantRepository.findByProgramIdAndUsername(programId, user.getUsername())
                .orElseThrow(NotFoundException::new);
        requireStatus(participant, ACTIVE_STATUSES, PARTICIPANT_CHECKIN_INVALID_STATUS);
        var program = programRepository.findById(programId).orElseThrow(NotFoundException::new);
        var language = resolveLanguage(user.getUsername());
        return aiClient.generateWeeklyPractices(
                participant.getFocusArea(), participant.getPersonalGoal(), program.getGoal(), language);
    }

    @PostMapping("/{programId}/assessment/mid")
    public void submitMidCycle(
            @PathVariable Long programId,
            @RequestBody @Valid AssessmentAnswersRequest request,
            @AuthenticationPrincipal UserDetails user) {
        var participant = participantRepository.findByProgramIdAndUsername(programId, user.getUsername())
                .orElseThrow(NotFoundException::new);
        requireStatus(participant, ACTIVE_STATUSES, PARTICIPANT_CHECKIN_INVALID_STATUS);
        var program = programRepository.findById(programId).orElseThrow(NotFoundException::new);
        requireAssessmentDue(participant, program, AssessmentResponse.Type.MID, PARTICIPANT_MID_CYCLE_NOT_DUE, PARTICIPANT_MID_CYCLE_ALREADY_COMPLETED);
        saveAssessmentResponse(participant, AssessmentResponse.Type.MID, request, null);
    }

    @GetMapping("/{programId}/assessment/mid/result")
    public MidCycleResultDto getMidCycleResult(
            @PathVariable Long programId,
            @AuthenticationPrincipal UserDetails user) {
        var participant = participantRepository.findByProgramIdAndUsername(programId, user.getUsername())
                .orElseThrow(NotFoundException::new);
        var baseline = assessmentResponseRepository.findByParticipantIdAndTypeAndCycle(
                participant.getId(), AssessmentResponse.Type.BASELINE, participant.getCurrentCycle())
                .orElseThrow(() -> new ApiValidationException(PARTICIPANT_MID_CYCLE_NO_BASELINE, "baseline", "missing", "Baseline assessment not found"));
        var mid = assessmentResponseRepository.findByParticipantIdAndTypeAndCycle(
                participant.getId(), AssessmentResponse.Type.MID, participant.getCurrentCycle())
                .orElseThrow(NotFoundException::new);
        var questions = findAndValidateQuestions(participant.getFocusArea());
        return MidCycleResultDto.of(participant.getFocusArea(), questions, baseline, mid);
    }

    @PostMapping("/{programId}/assessment/final")
    public void submitFinal(
            @PathVariable Long programId,
            @RequestBody @Valid AssessmentAnswersRequest request,
            @AuthenticationPrincipal UserDetails user) {
        var participant = participantRepository.findByProgramIdAndUsername(programId, user.getUsername())
                .orElseThrow(NotFoundException::new);
        requireStatus(participant, ACTIVE_STATUSES, PARTICIPANT_FINAL_INVALID_STATUS);
        var program = programRepository.findById(programId).orElseThrow(NotFoundException::new);
        requireAssessmentDue(participant, program, AssessmentResponse.Type.FINAL, PARTICIPANT_FINAL_NOT_DUE, PARTICIPANT_FINAL_ALREADY_COMPLETED);
        requireNps(request, participant, program);
        var isFinalCycle = isFinalCycle(participant, program);
        saveAssessmentResponse(participant, AssessmentResponse.Type.FINAL, request, isFinalCycle ? request.nps() : null);
        Optional.of(participant)
                .map(p -> isFinalCycle
                        ? p.setStatus(ProgramParticipant.Status.COMPLETED)
                        : p.incrementCycle().setStatus(ProgramParticipant.Status.ENROLLING))
                .ifPresent(participantRepository::save);
    }

    @GetMapping("/{programId}/journey")
    public JourneyDto getJourney(
            @PathVariable Long programId,
            @RequestParam int cycle,
            @AuthenticationPrincipal UserDetails user) {
        var journey = participantRepository.findJourneyData(programId, user.getUsername(), cycle)
                .orElseThrow(NotFoundException::new);
        var questions = findAndValidateQuestions(journey.focusArea());
        return JourneyDto.of(journey, questions);
    }

    @PostMapping("/{programId}/checkin")
    public void submitCheckin(
            @PathVariable Long programId,
            @RequestBody @Valid CheckinRequest request,
            @AuthenticationPrincipal UserDetails user) {
        var participant = participantRepository.findByProgramIdAndUsername(programId, user.getUsername())
                .orElseThrow(NotFoundException::new);
        requireStatus(participant, ACTIVE_STATUSES, PARTICIPANT_CHECKIN_INVALID_STATUS);
        var program = programRepository.findById(programId).orElseThrow(NotFoundException::new);
        var currentWeek = calculateCurrentWeek(participant, program);
        var practice = weeklyPracticeRepository
                .findByParticipantIdAndCycleAndWeekNumber(participant.getId(), participant.getCurrentCycle(), currentWeek)
                .orElseThrow(NotFoundException::new);
        practice.setFridayResponse(request.response())
                .setBlockerReason(request.blockerReason());
        weeklyPracticeRepository.save(practice);
    }

    private void requireStatus(ProgramParticipant participant, Set<ProgramParticipant.Status> allowed, String errorCode) {
        if (!allowed.contains(participant.getStatus())) {
            throw new ApiValidationException(errorCode, "status", participant.getStatus().name(), "Invalid participant status");
        }
    }

    private void requireAssessmentDue(ProgramParticipant participant, Program program, AssessmentResponse.Type type, String notDueCode, String completedCode) {
        var status = computeAssessmentStatus(participant, program, type);
        if (status == AssessmentStatus.NOT_DUE) {
            throw new ApiValidationException(notDueCode, "assessmentStatus", status.name(), type.name() + " assessment is not yet due");
        }
        if (status == AssessmentStatus.COMPLETED) {
            throw new ApiValidationException(completedCode, "assessmentStatus", status.name(), type.name() + " assessment already completed");
        }
    }

    private void requireNps(AssessmentAnswersRequest request, ProgramParticipant participant, Program program) {
        if (isFinalCycle(participant, program) && request.nps() == null) {
            throw new ApiValidationException(PARTICIPANT_FINAL_NPS_REQUIRED, "nps", "null", "NPS is required on the final cycle assessment");
        }
    }

    private void requireValidFocusArea(Program program, String focusArea) {
        if (!program.getFocusAreas().contains(focusArea)) {
            throw new ApiValidationException(PARTICIPANT_ENROLL_FOCUS_AREA_INVALID, "focusArea", focusArea, "Focus area is not available in this program");
        }
    }

    private String resolveLocale(String username) {
        return userRepository.findByUsername(username)
                .map(u -> Optional.ofNullable(u.getLocale()).orElse(UserUtils.defaultLocale()))
                .orElse(UserUtils.defaultLocale());
    }

    private String resolveLanguage(String username) {
        return UserUtils.localeToLanguage(resolveLocale(username));
    }

    private String resolveFallbackPractice(String username) {
        return FALLBACK_PRACTICE.getOrDefault(resolveLocale(username), FALLBACK_PRACTICE.get(UserUtils.defaultLocale()));
    }

    private void saveAssessmentResponse(
            ProgramParticipant participant, AssessmentResponse.Type type,
            AssessmentAnswersRequest request, Integer nps) {
        var response = new AssessmentResponse()
                .setParticipantId(participant.getId())
                .setType(type)
                .setCycle(participant.getCurrentCycle())
                .setFocusAreaKey(participant.getFocusArea())
                .setQ1(request.q1())
                .setQ2(request.q2())
                .setQ3(request.q3())
                .setQ4(request.q4())
                .setQ5(request.q5())
                .setOpenText(request.openText())
                .setNps(nps)
                .setCompletedAt(LocalDateTime.now());
        assessmentResponseRepository.save(response);
    }

    private void upsertAssessmentResponse(
            ProgramParticipant participant, AssessmentResponse.Type type,
            AssessmentAnswersRequest request) {
        var response = assessmentResponseRepository
                .findByParticipantIdAndTypeAndCycle(participant.getId(), type, participant.getCurrentCycle())
                .orElseGet(() -> new AssessmentResponse()
                        .setParticipantId(participant.getId())
                        .setType(type)
                        .setCycle(participant.getCurrentCycle())
                        .setFocusAreaKey(participant.getFocusArea()));
        response.setQ1(request.q1())
                .setQ2(request.q2())
                .setQ3(request.q3())
                .setQ4(request.q4())
                .setQ5(request.q5())
                .setOpenText(request.openText())
                .setCompletedAt(LocalDateTime.now());
        assessmentResponseRepository.save(response);
    }

    private List<AssessmentQuestion> findAndValidateQuestions(String focusAreaKey) {
        return assessmentQuestionRepository.findByFocusAreaKey(focusAreaKey);
    }

    private int calculateCurrentWeek(ProgramParticipant participant, Program program) {
        if (participant.getEnrolledAt() == null) {
            return FIRST_WEEK;
        }
        var cycleLengthDays = effectiveCycleLength(program);
        var cycleStartDay = (long) (participant.getCurrentCycle() - 1) * cycleLengthDays;
        var daysSinceEnrollment = Duration.between(participant.getEnrolledAt(), LocalDateTime.now()).toDays();
        var dayIntoCycle = Math.max(0, daysSinceEnrollment - cycleStartDay);
        var maxWeeks = Math.max(1, cycleLengthDays / DAYS_PER_WEEK);
        return (int) Math.min(dayIntoCycle / DAYS_PER_WEEK + 1, maxWeeks);
    }

    private boolean isCurrentWeekPractice(WeeklyPractice practice, ProgramParticipant participant, int currentWeek) {
        return practice.getCycle() == participant.getCurrentCycle() && practice.getWeekNumber() == currentWeek;
    }

    private WeeklyPractice ensureCurrentWeekPractice(ProgramParticipant participant, Program program) {
        var latest = weeklyPracticeRepository.findLatestByParticipantId(participant.getId()).orElse(null);
        if (latest == null) {
            return null;
        }
        var currentWeek = calculateCurrentWeek(participant, program);
        if (isCurrentWeekPractice(latest, participant, currentWeek)) {
            return latest;
        }
        try {
            var newPractice = new WeeklyPractice()
                    .setParticipantId(participant.getId())
                    .setCycle(participant.getCurrentCycle())
                    .setWeekNumber(currentWeek)
                    .setText(latest.getText())
                    .setSource(latest.getSource())
                    .setCreatedAt(LocalDateTime.now());
            return weeklyPracticeRepository.save(newPractice);
        } catch (DuplicateKeyException e) {
            return weeklyPracticeRepository.findByParticipantIdAndCycleAndWeekNumber(
                    participant.getId(), participant.getCurrentCycle(), currentWeek)
                    .orElse(latest);
        }
    }


    private int effectiveCycleLength(Program program) {
        return Optional.ofNullable(program.getCycleLengthDays())
                .filter(c -> c > 0)
                .orElse(Optional.ofNullable(program.getDurationDays()).orElse(DEFAULT_CYCLE_LENGTH_DAYS));
    }

    private int totalCycles(Program program) {
        var cycleLength = effectiveCycleLength(program);
        var duration = Optional.ofNullable(program.getDurationDays()).orElse(DEFAULT_CYCLE_LENGTH_DAYS);
        return Math.max(1, duration / cycleLength);
    }

    private boolean isFinalCycle(ProgramParticipant participant, Program program) {
        return participant.getCurrentCycle() >= totalCycles(program);
    }

    public enum AssessmentStatus {
        NOT_DUE, DUE, COMPLETED
    }

    public record DashboardDto(
            String programName,
            String programGoal,
            String focusArea,
            String personalGoal,
            int currentCycle,
            ProgramParticipant.Status participantStatus,
            PracticeDto practice,
            NextSessionDto nextSession,
            int sessionsConsumed,
            int sessionsAllocated,
            AssessmentStatus midCycleStatus,
            AssessmentStatus finalStatus,
            LearnMoreDto learnMore
    ) {
        static DashboardDto of(
                ProgramParticipant participant,
                Program program,
                WeeklyPractice practice,
                ScheduledSession nextSession,
                UserAllocation allocation,
                AssessmentStatus midCycleStatus,
                AssessmentStatus finalStatus,
                LearnMoreDto learnMore) {
            return new DashboardDto(
                    program.getName(),
                    program.getGoal(),
                    participant.getFocusArea(),
                    participant.getPersonalGoal(),
                    participant.getCurrentCycle(),
                    participant.getStatus(),
                    practice != null ? new PracticeDto(practice.getText(), practice.getSource(), practice.getFridayResponse(), practice.getWeekNumber()) : null,
                    nextSession != null ? new NextSessionDto(nextSession.getId(), nextSession.getTime(), nextSession.getCoachUsername()) : null,
                    allocation != null ? Optional.ofNullable(allocation.getConsumedUnits()).orElse(0) : 0,
                    allocation != null ? Optional.ofNullable(allocation.getAllocatedUnits()).orElse(0) : 0,
                    midCycleStatus,
                    finalStatus,
                    learnMore != null && !learnMore.isEmpty() ? learnMore : null
            );
        }
    }

    public record PracticeDto(String text, WeeklyPractice.Source source, WeeklyPractice.FridayResponse fridayResponse, int weekNumber) {}

    public record NextSessionDto(Long id, LocalDateTime time, String coachUsername) {}

    public record EnrollmentInfoDto(
            String programGoal,
            Set<String> focusAreas,
            String selectedFocusArea,
            String personalGoal
    ) {}

    public record EnrollRequest(@NotBlank String focusArea, String personalGoal) {}

    public record PracticeUpdateRequest(@NotNull @Min(1) Integer weekNumber, @NotBlank String text) {}

    public record CheckinRequest(@NotNull WeeklyPractice.FridayResponse response, String blockerReason) {}

    public record QuestionDto(int order, String text) {}

    public record AssessmentQuestionsDto(
            String focusAreaKey,
            List<QuestionDto> questions,
            Integer q1, Integer q2, Integer q3, Integer q4, Integer q5,
            String openText
    ) {
        static AssessmentQuestionsDto of(String focusAreaKey, List<AssessmentQuestion> questions, AssessmentResponse existing) {
            return new AssessmentQuestionsDto(
                    focusAreaKey,
                    questions.stream().map(q -> new QuestionDto(q.getQuestionOrder(), q.getQuestionText())).toList(),
                    existing != null ? existing.getQ1() : null,
                    existing != null ? existing.getQ2() : null,
                    existing != null ? existing.getQ3() : null,
                    existing != null ? existing.getQ4() : null,
                    existing != null ? existing.getQ5() : null,
                    existing != null ? existing.getOpenText() : null
            );
        }
    }

    public record AssessmentAnswersRequest(
            @NotNull @Min(1) @Max(5) Integer q1,
            @NotNull @Min(1) @Max(5) Integer q2,
            @NotNull @Min(1) @Max(5) Integer q3,
            @NotNull @Min(1) @Max(5) Integer q4,
            @NotNull @Min(1) @Max(5) Integer q5,
            String openText,
            @Min(0) @Max(10) Integer nps
    ) {}

    public record MidCycleResultDto(
            String focusAreaKey,
            List<QuestionResultDto> questions
    ) {
        static MidCycleResultDto of(String focusAreaKey, List<AssessmentQuestion> questions,
                                     AssessmentResponse baseline, AssessmentResponse mid) {
            var baselineRatings = List.of(baseline.getQ1(), baseline.getQ2(), baseline.getQ3(), baseline.getQ4(), baseline.getQ5());
            var midRatings = List.of(mid.getQ1(), mid.getQ2(), mid.getQ3(), mid.getQ4(), mid.getQ5());
            return new MidCycleResultDto(
                    focusAreaKey,
                    IntStream.range(0, questions.size())
                            .mapToObj(i -> new QuestionResultDto(
                                    questions.get(i).getQuestionOrder(),
                                    questions.get(i).getQuestionText(),
                                    baselineRatings.get(i),
                                    midRatings.get(i)
                            )).toList()
            );
        }
    }

    public record QuestionResultDto(int order, String text, int baseline, int mid) {}

    public record JourneyDto(
            String programName,
            String focusAreaKey,
            String personalGoal,
            boolean isFinalCycle,
            List<JourneyQuestionDto> questions,
            JourneyStatsDto stats
    ) {
        static JourneyDto of(JourneyProjection j, List<AssessmentQuestion> questions) {
            var baselineRatings = List.of(j.baselineQ1(), j.baselineQ2(), j.baselineQ3(), j.baselineQ4(), j.baselineQ5());
            var hasMid = j.midQ1() != null;
            var midRatings = hasMid ? List.of(j.midQ1(), j.midQ2(), j.midQ3(), j.midQ4(), j.midQ5()) : null;
            var finalRatings = List.of(j.finalQ1(), j.finalQ2(), j.finalQ3(), j.finalQ4(), j.finalQ5());
            var avgBaseline = baselineRatings.stream().mapToInt(Integer::intValue).average().orElse(0);
            var avgFinal = finalRatings.stream().mapToInt(Integer::intValue).average().orElse(0);
            return new JourneyDto(
                    j.programName(),
                    j.focusArea(),
                    j.personalGoal(),
                    j.isFinalCycle(),
                    IntStream.range(0, questions.size())
                            .mapToObj(i -> new JourneyQuestionDto(
                                    questions.get(i).getQuestionOrder(),
                                    questions.get(i).getQuestionText(),
                                    baselineRatings.get(i),
                                    midRatings != null ? midRatings.get(i) : null,
                                    finalRatings.get(i),
                                    finalRatings.get(i) - baselineRatings.get(i)
                            )).toList(),
                    new JourneyStatsDto(
                            Optional.ofNullable(j.consumedUnits()).orElse(0),
                            Optional.ofNullable(j.allocatedUnits()).orElse(0),
                            j.practicesTotal(),
                            j.practicesResponded(),
                            Math.round((avgFinal - avgBaseline) * 10.0) / 10.0
                    )
            );
        }
    }

    public record JourneyQuestionDto(int order, String text, int baseline, Integer mid, int end, int delta) {}

    public record JourneyStatsDto(int sessionsConsumed, int sessionsAllocated,
                                   long practicesTotal, long practicesResponded, double avgImprovement) {}

    public record ProgramStatusDto(
            boolean enrolled,
            Long programId,
            String programName,
            String programGoal,
            int durationDays,
            int sessionsPerParticipant,
            ProgramParticipant.Status participantStatus
    ) {
        static ProgramStatusDto none() {
            return new ProgramStatusDto(false, null, null, null, 0, 0, null);
        }

        static ProgramStatusDto of(ProgramParticipant p, Program program) {
            return new ProgramStatusDto(
                    true,
                    program.getId(),
                    program.getName(),
                    program.getGoal(),
                    Optional.ofNullable(program.getDurationDays()).orElse(0),
                    Optional.ofNullable(program.getSessionsPerParticipant()).orElse(0),
                    p.getStatus()
            );
        }
    }
}
