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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.topleader.topleader.common.exception.ErrorCodeConstants.ENROLLMENT_EMAIL_INVALID_STATUS;

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
        var isFuture = Optional.ofNullable(pkg.getValidFrom())
                .filter(vf -> vf.isAfter(now))
                .isPresent();
        var scheduledAt = isFuture ? pkg.getValidFrom() : now;
        var participants = participantRepository.findByProgramId(program.getId()).stream()
                .filter(p -> p.getEnrollmentEmailSentAt() == null)
                .toList();

        log.info("Scheduling enrollment emails for program {} — {} participants, isFuture={}",
                program.getId(), participants.size(), isFuture);

        participants.forEach(p -> {
            participantRepository.save(p.setEnrollmentEmailScheduledAt(scheduledAt));
            if (!isFuture) {
                sendEnrollmentEmail(p, program);
            }
        });
    }

    public void processScheduledEmails() {
        var now = LocalDateTime.now();
        var pending = participantRepository.findPendingEnrollmentEmails(now);
        log.info("Processing {} pending enrollment emails", pending.size());

        pending.forEach(participant -> {
            var program = programRepository.findById(participant.getProgramId())
                    .orElse(null);
            if (program == null) {
                log.warn("Program {} not found for participant {}", participant.getProgramId(), participant.getUsername());
                return;
            }
            sendEnrollmentEmail(participant, program);
        });
    }

    public void resendEnrollmentEmail(Long programId, String username) {
        var participant = participantRepository.findByProgramIdAndUsername(programId, username)
                .orElseThrow(NotFoundException::new);
        if (participant.getStatus() != ProgramParticipant.Status.INVITED) {
            throw new ApiValidationException(ENROLLMENT_EMAIL_INVALID_STATUS, "status",
                    participant.getStatus().name(), "Can only resend enrollment email for INVITED participants");
        }
        var program = programRepository.findById(programId).orElseThrow(NotFoundException::new);

        participant.setEnrollmentEmailSentAt(null)
                .setEnrollmentEmailScheduledAt(LocalDateTime.now());
        participantRepository.save(participant);

        sendEnrollmentEmail(participant, program);
    }

    void sendEnrollmentEmail(ProgramParticipant participant, Program program) {
        var user = userRepository.findByUsername(participant.getUsername()).orElse(null);
        if (user == null) {
            log.warn("User {} not found, skipping enrollment email", participant.getUsername());
            return;
        }

        var isNew = participant.isNewUser();
        var hrUser = userRepository.findByUsername(program.getCreatedBy()).orElse(User.empty());
        var pkg = coachingPackageService.getPackageEntity(program.getCoachingPackageId());

        var firstName = Optional.ofNullable(user.getFirstName())
                .filter(StringUtils::isNotBlank)
                .orElse("there");
        var hrName = buildFullName(hrUser.getFirstName(), hrUser.getLastName());
        var hrEmail = Optional.ofNullable(hrUser.getEmail())
                .filter(StringUtils::isNotBlank)
                .orElse(hrUser.getUsername());
        var startDate = Optional.ofNullable(pkg.getValidFrom())
                .map(DATE_FORMAT::format)
                .orElse(StringUtils.EMPTY);

        var ctaUrl = isNew
                ? buildNewUserCtaUrl(user.getUsername(), program.getId())
                : String.format("%s/#/program-enrollment/%d", appUrl, program.getId());

        var locale = resolveLocale(user.getLocale());
        var templatePath = isNew
                ? "templates/enrollment/enrollment-new-" + locale + ".html"
                : "templates/enrollment/enrollment-existing-" + locale + ".html";

        var subjects = isNew ? NEW_USER_SUBJECTS : EXISTING_USER_SUBJECTS;
        var subject = String.format(
                subjects.getOrDefault(locale, subjects.get(defaultLocale)),
                program.getName());

        var params = new HashMap<>(Map.<String, Object>of(
                "firstName", firstName,
                "programName", Optional.ofNullable(program.getName()).orElse(StringUtils.EMPTY),
                "programGoal", Optional.ofNullable(program.getGoal()).orElse(StringUtils.EMPTY),
                "durationDays", Optional.ofNullable(program.getDurationDays()).map(String::valueOf).orElse(StringUtils.EMPTY),
                "sessionsPerParticipant", Optional.ofNullable(program.getSessionsPerParticipant()).map(String::valueOf).orElse(StringUtils.EMPTY),
                "startDate", startDate,
                "hrName", hrName,
                "hrEmail", hrEmail,
                "ctaUrl", ctaUrl));

        var body = templating.getMessage(params, templatePath);
        var email = Optional.ofNullable(user.getEmail())
                .filter(StringUtils::isNotBlank)
                .orElse(user.getUsername());

        emailService.sendEmail(email, subject, body);

        participant.setEnrollmentEmailSentAt(LocalDateTime.now());
        participantRepository.save(participant);

        log.info("Sent enrollment email to {} for program {} (newUser={})", email, program.getId(), isNew);
    }

    private String buildNewUserCtaUrl(String username, Long programId) {
        var token = tokenService.generateToken();
        tokenService.saveToken(new Token()
                .setToken(token)
                .setUsername(username)
                .setType(Token.Type.SET_PASSWORD));
        return String.format("%s/#/create-password?token=%s&redirect=/program-enrollment/%d",
                appUrl, token, programId);
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
