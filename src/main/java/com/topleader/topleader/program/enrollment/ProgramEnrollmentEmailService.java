package com.topleader.topleader.program.enrollment;

import com.topleader.topleader.common.email.Emailing;
import com.topleader.topleader.common.email.Templating;
import com.topleader.topleader.common.exception.ApiValidationException;
import com.topleader.topleader.common.exception.NotFoundException;
import com.topleader.topleader.program.Program;
import com.topleader.topleader.program.ProgramRepository;
import com.topleader.topleader.program.participant.ProgramParticipant;
import com.topleader.topleader.program.participant.ProgramParticipantRepository;
import com.topleader.topleader.session.coaching_package.CoachingPackageService;
import com.topleader.topleader.user.User;
import com.topleader.topleader.user.UserRepository;
import com.topleader.topleader.user.token.Token;
import com.topleader.topleader.user.token.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.topleader.topleader.common.exception.ErrorCodeConstants.ENROLLMENT_EMAIL_INVALID_STATUS;
import static com.topleader.topleader.common.exception.ErrorCodeConstants.NOT_PART_OF_COMPANY;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProgramEnrollmentEmailService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy");

    private static final Map<String, String> EXISTING_USER_SUBJECTS = Map.of(
            "en", "You've been enrolled in %s",
            "cs", "Byli jste zapsáni do programu %s",
            "fr", "Vous êtes inscrit(e) au programme %s",
            "de", "Sie sind für %s eingeschrieben");

    private static final Map<String, String> NEW_USER_SUBJECTS = Map.of(
            "en", "You've been invited to %s",
            "cs", "Byli jste pozváni do programu %s",
            "fr", "Vous êtes invité(e) au programme %s",
            "de", "Sie wurden zu %s eingeladen");

    private final ProgramRepository programRepository;
    private final ProgramParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final CoachingPackageService coachingPackageService;
    private final Emailing emailService;
    private final Templating templating;
    private final TokenService tokenService;

    @Value("${top-leader.app-url}")
    private String appUrl;

    @Value("${top-leader.supported-invitations}")
    private List<String> supportedLocales;

    @Value("${top-leader.default-locale}")
    private String defaultLocale;

    public void scheduleEnrollmentEmails(Program program) {
        var pkg = coachingPackageService.getPackageEntity(program.getCoachingPackageId());
        var now = LocalDateTime.now();
        var today = now.toLocalDate();
        var scheduledAt = Optional.ofNullable(pkg.getValidFrom())
                .filter(vf -> vf.toLocalDate().isAfter(today))
                .orElse(now);
        var sendImmediately = !scheduledAt.isAfter(now);

        var participants = participantRepository.findByProgramId(program.getId()).stream()
                .filter(p -> p.getEnrollmentEmailSentAt() == null)
                .toList();
        participants.forEach(p -> participantRepository.save(p.setEnrollmentEmailScheduledAt(scheduledAt)));

        log.info("Scheduling enrollment emails for program {} — {} participants, sendImmediately={}",
                program.getId(), participants.size(), sendImmediately);

        if (sendImmediately) {
            participants.forEach(p -> sendEnrollmentEmail(p, program));
        }
    }

    public void processScheduledEmails() {
        var now = LocalDateTime.now();
        var pending = participantRepository.findPendingEnrollmentEmails(now);
        log.info("Processing {} pending enrollment emails", pending.size());
        pending.forEach(this::sendEmail);
    }

    public void resendEnrollmentEmail(Long programId, String username, Long companyId) {
        var program = validateResend(programId, username, companyId);
        var participant = participantRepository.findByProgramIdAndUsername(programId, username).orElseThrow();

        participant.setEnrollmentEmailSentAt(null)
                .setEnrollmentEmailScheduledAt(LocalDateTime.now());
        participantRepository.save(participant);

        sendEnrollmentEmail(participant, program);
    }

    private Program validateResend(Long programId, String username, Long companyId) {
        programRepository.findByIdAndCompanyId(programId, companyId)
                .orElseThrow(() -> new ApiValidationException(NOT_PART_OF_COMPANY, "programId",
                        String.valueOf(programId), "Program does not belong to user's company"));
        var participant = participantRepository.findByProgramIdAndUsername(programId, username)
                .orElseThrow(NotFoundException::new);
        if (participant.getStatus() != ProgramParticipant.Status.INVITED) {
            throw new ApiValidationException(ENROLLMENT_EMAIL_INVALID_STATUS, "status",
                    participant.getStatus().name(), "Can only resend enrollment email for INVITED participants");
        }
        return programRepository.findById(programId).orElseThrow(NotFoundException::new);
    }

    void sendEnrollmentEmail(ProgramParticipant participant, Program program) {
        var usersByUsername = userRepository.findAllByUsernameIn(List.of(participant.getUsername(), program.getCreatedBy())).stream()
                .collect(Collectors.toMap(User::getUsername, Function.identity()));
        var user = usersByUsername.get(participant.getUsername());
        if (user == null) {
            log.warn("User {} not found, skipping enrollment email", participant.getUsername());
            return;
        }
        var hrUser = Optional.ofNullable(usersByUsername.get(program.getCreatedBy())).orElse(User.empty());
        var pkg = coachingPackageService.getPackageEntity(program.getCoachingPackageId());

        sendEmail(PendingEnrollmentEmailRow.builder()
                .participantId(participant.getId())
                .programId(program.getId())
                .username(participant.getUsername())
                .newUser(participant.isNewUser())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .locale(user.getLocale())
                .programName(program.getName())
                .programGoal(program.getGoal())
                .durationDays(program.getDurationDays())
                .sessionsPerParticipant(program.getSessionsPerParticipant())
                .hrUsername(program.getCreatedBy())
                .validFrom(pkg.getValidFrom())
                .hrFirstName(hrUser.getFirstName())
                .hrLastName(hrUser.getLastName())
                .hrEmail(hrUser.getEmail())
                .build());
    }

    private void sendEmail(PendingEnrollmentEmailRow row) {
        try {
            var firstName = Optional.ofNullable(row.firstName())
                    .filter(StringUtils::isNotBlank)
                    .orElse("there");
            var hrName = buildFullName(row.hrFirstName(), row.hrLastName());
            var hrEmail = Optional.ofNullable(row.hrEmail())
                    .filter(StringUtils::isNotBlank)
                    .orElse(Optional.ofNullable(row.hrUsername()).orElse(StringUtils.EMPTY));
            var startDate = Optional.ofNullable(row.validFrom())
                    .map(DATE_FORMAT::format)
                    .orElse(StringUtils.EMPTY);

            var ctaUrl = row.newUser()
                    ? buildNewUserCtaUrl(row.username(), row.programId())
                    : String.format("%s/#/program-enrollment/%d", appUrl, row.programId());

            var locale = resolveLocale(row.locale());
            var templatePath = row.newUser()
                    ? "templates/enrollment/enrollment-new-" + locale + ".html"
                    : "templates/enrollment/enrollment-existing-" + locale + ".html";

            var subjects = row.newUser() ? NEW_USER_SUBJECTS : EXISTING_USER_SUBJECTS;
            var subject = String.format(
                    subjects.getOrDefault(locale, subjects.get(defaultLocale)),
                    row.programName());

            var params = Map.<String, Object>of(
                    "firstName", firstName,
                    "programName", Optional.ofNullable(row.programName()).orElse(StringUtils.EMPTY),
                    "programGoal", Optional.ofNullable(row.programGoal()).orElse(StringUtils.EMPTY),
                    "durationDays", Optional.ofNullable(row.durationDays()).map(String::valueOf).orElse(StringUtils.EMPTY),
                    "sessionsPerParticipant", Optional.ofNullable(row.sessionsPerParticipant()).map(String::valueOf).orElse(StringUtils.EMPTY),
                    "startDate", startDate,
                    "hrName", hrName,
                    "hrEmail", hrEmail,
                    "ctaUrl", ctaUrl);

            var body = templating.getMessage(params, templatePath);
            var email = Optional.ofNullable(row.email())
                    .filter(StringUtils::isNotBlank)
                    .orElse(row.username());

            emailService.sendEmail(email, subject, body);

            participantRepository.findById(row.participantId()).ifPresent(p -> {
                p.setEnrollmentEmailSentAt(LocalDateTime.now());
                participantRepository.save(p);
            });

            log.info("Sent enrollment email to {} for program {} (newUser={})", email, row.programId(), row.newUser());
        } catch (Exception e) {
            log.error("Failed to send enrollment email to {} for program {}", row.username(), row.programId(), e);
        }
    }

    private String buildNewUserCtaUrl(String username, Long programId) {
        var token = tokenService.generateToken();
        tokenService.saveToken(new Token()
                .setToken(token)
                .setUsername(username)
                .setType(Token.Type.SET_PASSWORD));
        return String.format("%s/#/create-password?token=%s&redirect=/program-enrollment/%d",
                appUrl, URLEncoder.encode(token, StandardCharsets.UTF_8), programId);
    }

    private String buildFullName(String firstName, String lastName) {
        return Optional.ofNullable(firstName).orElse(StringUtils.EMPTY)
                + (StringUtils.isNotBlank(firstName) && StringUtils.isNotBlank(lastName) ? " " : StringUtils.EMPTY)
                + Optional.ofNullable(lastName).orElse(StringUtils.EMPTY);
    }

    private String resolveLocale(String locale) {
        return supportedLocales.contains(locale) ? locale : defaultLocale;
    }
}
