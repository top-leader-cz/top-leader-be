package com.topleader.topleader.common.ai;

import com.topleader.topleader.coach.list.CoachListView;
import com.topleader.topleader.coach.list.CoachListViewRepository;
import com.topleader.topleader.history.DataHistory;
import com.topleader.topleader.history.DataHistoryRepository;
import com.topleader.topleader.history.data.UserSessionStoredData;
import com.topleader.topleader.user.UserRepository;
import com.topleader.topleader.user.userinfo.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class McpToolsConfig {

    private final UserRepository userRepository;
    private final UserInfoRepository userInfoRepository;
    private final CoachListViewRepository coachListViewRepository;
    private final DataHistoryRepository dataHistoryRepository;
    private final RestClient restClient;

    @Value("${tavily.api-key:}")
    private String tavilyApiKey;

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

    public record TavilySearchRequest(String query) {}

    public record TavilySearchResult(String title, String url, String content) {}

    @Bean
    @Description("Search the web for real articles related to a topic. Returns article titles, URLs, and content snippets. Use this to find real articles with valid URLs for user recommendations.")
    public Function<TavilySearchRequest, List<TavilySearchResult>> searchArticles() {
        return request -> tavilySearch(request.query(), List.of(), 10);
    }

    @Bean
    @Description("Search for real YouTube videos related to a topic. Returns video titles, YouTube URLs, and descriptions. Use this to find real videos with valid URLs.")
    public Function<TavilySearchRequest, List<TavilySearchResult>> searchVideos() {
        return request -> tavilySearch(request.query(), List.of("youtube.com"), 10);
    }

    private List<TavilySearchResult> tavilySearch(String query, List<String> includeDomains, int maxResults) {
        log.info("Tavily search: {}", query);
        var body = new java.util.HashMap<String, Object>(Map.of(
                "query", query,
                "search_depth", "basic",
                "max_results", maxResults
        ));
        if (!includeDomains.isEmpty()) {
            body.put("include_domains", includeDomains);
        }

        var response = restClient.post()
                .uri("https://api.tavily.com/search")
                .header("Authorization", "Bearer " + tavilyApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});

        @SuppressWarnings("unchecked")
        var results = (List<Map<String, Object>>) response.get("results");

        var items = Optional.ofNullable(results).orElse(List.of()).stream()
                .map(r -> new TavilySearchResult(
                        (String) r.get("title"),
                        (String) r.get("url"),
                        (String) r.get("content")
                ))
                .toList();

        log.info("Tavily results for '{}': {} items", query, items.size());
        return items;
    }
}
