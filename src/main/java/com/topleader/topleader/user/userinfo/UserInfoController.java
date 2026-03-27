package com.topleader.topleader.user.userinfo;

import com.topleader.topleader.common.email.EmailTemplateService;
import com.topleader.topleader.common.event.CoachChangedEvent;
import com.topleader.topleader.common.exception.ApiValidationException;
import com.topleader.topleader.common.exception.NotFoundException;
import com.topleader.topleader.hr.company.Company;
import com.topleader.topleader.hr.company.CompanyRepository;
import com.topleader.topleader.common.notification.Notification;
import com.topleader.topleader.common.notification.NotificationService;
import com.topleader.topleader.common.notification.context.CoachLinkedNotificationContext;
import com.topleader.topleader.user.User;
import com.topleader.topleader.user.UserRepository;
import com.topleader.topleader.user.userinsight.UserInsightService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static java.util.Objects.nonNull;
import static java.util.function.Predicate.not;


/**
 * @author Daniel Slavik
 */
@RestController
@RequestMapping("/api/latest/user-info")
@RequiredArgsConstructor
public class UserInfoController {

    private final UserInfoService userInfoService;

    private final UserRepository userRepository;

    private final NotificationService notificationService;

    private final UserInsightService userInsightService;

    private final CompanyRepository companyRepository;

    private final EmailTemplateService emailTemplateService;

    private final ApplicationEventPublisher eventPublisher;


    @GetMapping
    public UserInfoDto getUserInfo(@AuthenticationPrincipal UserDetails user) {

        final var dbUser = userRepository.findByUsername(user.getUsername()).orElseThrow(NotFoundException::new);
        final var company = Optional.ofNullable(dbUser.getCompanyId()).flatMap(companyRepository::findById);

        return UserInfoDto.from(
            userInfoService.find(user.getUsername()),
            dbUser,
            company
        );
    }

    @PostMapping("/locale")
    public UserInfoDto setLocale(@AuthenticationPrincipal UserDetails user, @RequestBody @Valid SetLocaleRequestDto request) {

        final var dbUser = userRepository.findByUsername(user.getUsername())
            .map(u -> u.setLocale(request.locale()))
            .map(userRepository::save)
            .orElseThrow(NotFoundException::new);
        final var company = Optional.ofNullable(dbUser.getCompanyId()).flatMap(companyRepository::findById);

        return UserInfoDto.from(
            userInfoService.find(user.getUsername()),
            dbUser,
            company
        );
    }

    @PostMapping("/notes")
    public SetNotesRequestDto setNotes(@AuthenticationPrincipal UserDetails user, @RequestBody @Valid SetNotesRequestDto request) {

        return new SetNotesRequestDto(
            userInfoService.setNotes(user.getUsername(), request.notes())
                .getNotes()
        );
    }

    @GetMapping("/notes")
    public SetNotesRequestDto getNotes(@AuthenticationPrincipal UserDetails user) {
        return new SetNotesRequestDto(
            userInfoService.find(user.getUsername())
                .getNotes()
        );
    }

    @PostMapping("/strengths")
    public UserInfoDto setStrengths(@AuthenticationPrincipal UserDetails user, @RequestBody @Valid ListDataRequestDto request) {
        var userInfo = userInfoService.setStrengths(user.getUsername(), request.data());
        if (shouldQueryAi(userInfo)) {
            userInsightService.setUserInsight(userInfo);
        }

        final var dbUser = userRepository.findByUsername(user.getUsername()).orElseThrow(NotFoundException::new);
        final var company = Optional.ofNullable(dbUser.getCompanyId()).flatMap(companyRepository::findById);

        return UserInfoDto.from(userInfo, dbUser, company);
    }


    @PostMapping("/values")
    public UserInfoDto setValues(@AuthenticationPrincipal UserDetails user, @RequestBody @Valid ListDataRequestDto request) {
        var userInfo = userInfoService.setValues(user.getUsername(), request.data());
        if (shouldQueryAi(userInfo)) {
            userInsightService.setUserInsight(userInfo);
        }

        final var dbUser = userRepository.findByUsername(user.getUsername()).orElseThrow(NotFoundException::new);
        final var company = Optional.ofNullable(dbUser.getCompanyId()).flatMap(companyRepository::findById);

        return UserInfoDto.from(userInfo, dbUser, company);
    }

    @PostMapping("/timezone")
    public UserInfoDto setValues(@AuthenticationPrincipal UserDetails user, @RequestBody @Valid SetTimezoneRequestDto request) {

        final var dbUser = userRepository.findByUsername(user.getUsername())
            .map(u -> u.setTimeZone(request.timezone()))
            .map(userRepository::save)
            .orElseThrow(NotFoundException::new);
        final var company = Optional.ofNullable(dbUser.getCompanyId()).flatMap(companyRepository::findById);

        return UserInfoDto.from(
            userInfoService.find(user.getUsername()),
            dbUser,
            company
        );
    }

    @Transactional
    @PostMapping("/coach")
    public UserInfoDto setCoach(@AuthenticationPrincipal UserDetails user, @RequestBody @Valid SetCoachRequestDto request) {

        final var currentUser = userRepository.findByUsername(user.getUsername()).orElseThrow(NotFoundException::new);

        if (!Objects.equals(request.coach(), currentUser.getCoach())) {
            currentUser.setCoach(request.coach());
            userRepository.save(currentUser);
            eventPublisher.publishEvent(new CoachChangedEvent(user.getUsername()));
            if (nonNull(request.coach())) {
                emailTemplateService.sentPickedMessage(request.coach());
                notificationService.addNotification(new NotificationService.CreateNotificationRequest(
                    request.coach(),
                    Notification.Type.COACH_LINKED,
                    new CoachLinkedNotificationContext().setUsername(user.getUsername())
                ));
            }
        }

        final var dbUser = userRepository.findByUsername(user.getUsername())
            .map(u -> u.setCoach(request.coach()))
            .map(userRepository::save)
            .orElseThrow(NotFoundException::new);
        final var company = Optional.ofNullable(dbUser.getCompanyId()).flatMap(companyRepository::findById);

        return UserInfoDto.from(
            userInfoService.find(user.getUsername()),
            dbUser,
            company
        );
    }

    public record SetNotesRequestDto(String notes) {}

    public record SetLocaleRequestDto(@Pattern(regexp = "[a-z]{2}") String locale) {}

    public record SetCoachRequestDto(String coach) {}

    public record ListDataRequestDto(@NotEmpty List<String> data) {}

    public record SetTimezoneRequestDto(@Size(min = 1, max = 20) String timezone) {}

    public record UserInfoDto(
        String username,
        String firstName,
        String lastName,
        Set<User.Authority> userRoles,
        String timeZone,
        List<String> strengths,
        List<String> values,
        List<String> areaOfDevelopment,
        String coach,
        String locale,
        Set<String> allowedCoachRates,
        Long companyId,
        boolean programsEnabled
    ) {
        public static UserInfoDto from(UserInfo info, User user, Optional<Company> company) {
            return new UserInfoDto(
                info.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getAuthorities(),
                user.getTimeZone(),
                info.getStrengths(),
                info.getValues(),
                info.getAreaOfDevelopment(),
                user.getCoach(),
                user.getLocale(),
                Optional.ofNullable(user.getAllowedCoachRates())
                    .filter(not(CollectionUtils::isEmpty))
                    .orElse(company.map(Company::getAllowedCoachRates).orElse(null)),
                user.getCompanyId(),
                company.map(Company::isProgramsEnabled).orElse(false)
            );
        }
    }

    private boolean shouldQueryAi(UserInfo userInfo) {
        return !CollectionUtils.isEmpty(userInfo.getValues()) && !CollectionUtils.isEmpty(userInfo.getStrengths());
    }
}
