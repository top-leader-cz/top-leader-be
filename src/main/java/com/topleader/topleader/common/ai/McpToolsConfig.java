package com.topleader.topleader.common.ai;

import com.topleader.topleader.coach.list.CoachListView;
import com.topleader.topleader.coach.list.CoachListViewRepository;
import com.topleader.topleader.history.DataHistory;
import com.topleader.topleader.history.DataHistoryRepository;
import com.topleader.topleader.history.data.UserSessionStoredData;
import com.topleader.topleader.user.UserRepository;
import com.topleader.topleader.user.userinfo.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Configuration
@RequiredArgsConstructor
public class McpToolsConfig {

    private final UserRepository userRepository;
    private final UserInfoRepository userInfoRepository;
    private final CoachListViewRepository coachListViewRepository;
    private final DataHistoryRepository dataHistoryRepository;

    public record UserProfileRequest(String username) {}

    public record SessionHistoryEntry(
            LocalDateTime createdAt,
            List<String> areaOfDevelopment,
            String longTermGoal,
            String motivation,
            String reflection,
            List<UserSessionStoredData.ActionStepData> actionSteps
    ) {}

    public record UserProfileResponse(
            String username,
            String firstName,
            String lastName,
            List<String> strengths,
            List<String> values,
            List<String> areaOfDevelopment,
            String longTermGoal,
            List<SessionHistoryEntry> sessionHistory
    ) {}

    @Bean
    @Description("Get user profile including their strengths, values, development areas, and session history. Session history contains past coaching sessions with motivation, reflection, and action steps. Use this to personalize coaching advice.")
    public Function<UserProfileRequest, UserProfileResponse> getUserProfile() {
        return request -> {
            var user = userRepository.findByUsername(request.username()).orElse(null);
            var userInfo = userInfoRepository.findByUsername(request.username()).orElse(null);

            if (user == null) {
                return null;
            }

            var sessions = dataHistoryRepository
                    .findByUsernameAndType(request.username(), DataHistory.Type.USER_SESSION.name())
                    .stream()
                    .filter(h -> h.getData() instanceof UserSessionStoredData)
                    .map(h -> {
                        var data = (UserSessionStoredData) h.getData();
                        return new SessionHistoryEntry(
                                h.getCreatedAt(),
                                data.getAreaOfDevelopment(),
                                data.getLongTermGoal(),
                                data.getMotivation(),
                                data.getReflection(),
                                data.getActionSteps()
                        );
                    })
                    .toList();

            return new UserProfileResponse(
                    user.getUsername(),
                    user.getFirstName(),
                    user.getLastName(),
                    userInfo != null ? userInfo.getStrengths() : List.of(),
                    userInfo != null ? userInfo.getValues() : List.of(),
                    userInfo != null ? userInfo.getAreaOfDevelopment() : List.of(),
                    userInfo != null ? userInfo.getLongTermGoal() : null,
                    sessions
            );
        };
    }

    public record CoachResponse(
            String firstName,
            String lastName,
            String bio,
            String certificate,
            String primaryRoles,
            String fields,
            String languages,
            String experienceSince,
            String rate,
            String email,
            String linkedinProfile
    ) {
        static CoachResponse from(CoachListView c) {
            return new CoachResponse(
                    c.getFirstName(), c.getLastName(), c.getBio(),
                    c.getCertificate(), c.getPrimaryRoles(), c.getFields(),
                    c.getLanguages(), Optional.ofNullable(c.getExperienceSince()).map(Object::toString).orElse(null),
                    c.getRate(), c.getEmail(), c.getLinkedinProfile());
        }
    }

    public record CoachSearchRequest(String language, int limit) {}

    @Bean
    @Description("Search for available coaches. Returns coaches with: firstName, lastName, bio (coaching philosophy), certificate (certifications), primaryRoles (COACH, MENTOR, THERAPIST), fields (areas of expertise), experienceSince, languages, and rate.")
    public Function<CoachSearchRequest, List<CoachResponse>> searchCoaches() {
        return request -> coachListViewRepository.findAll(PageRequest.of(0, request.limit()))
                .stream()
                .map(CoachResponse::from)
                .toList();
    }

    public record CoachByNameRequest(String firstName, String lastName) {}

    @Bean
    @Description("Get detailed information about a specific coach by their first and last name. Returns bio, certificate, primaryRoles, fields, experienceSince, languages, rate, and contact details.")
    public Function<CoachByNameRequest, Optional<CoachResponse>> getCoachByName() {
        return request -> coachListViewRepository.findAll(PageRequest.of(0, 100))
                .stream()
                .filter(c -> request.firstName().equalsIgnoreCase(c.getFirstName())
                        && request.lastName().equalsIgnoreCase(c.getLastName()))
                .findFirst()
                .map(CoachResponse::from);
    }
}
