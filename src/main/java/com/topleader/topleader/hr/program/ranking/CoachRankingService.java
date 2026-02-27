package com.topleader.topleader.hr.program.ranking;

import com.topleader.topleader.coach.Coach;
import com.topleader.topleader.common.ai.AiClient;
import com.topleader.topleader.hr.program.dto.CoachMatchRequest;
import com.topleader.topleader.hr.program.dto.CoachPreviewDto;
import com.topleader.topleader.hr.program.dto.CoachPreviewResponse;
import com.topleader.topleader.user.User;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

@Service
@RequiredArgsConstructor
public class CoachRankingService {

    private final AiClient aiClient;

    // AI ranking result — preserves order (index = relevance rank) and reasons
    public record AiRankingResult(Map<String, String> reasons, Map<String, Integer> rankOrder, List<String> rankedUsernames) {}

    // Calls AI to rank candidates by relevance — returns ordered result, best first
    public AiRankingResult rank(List<Coach> candidates, Map<String, User> usersMap, CoachMatchRequest request, int topN) {
        var candidateUsernames = candidates.stream().map(Coach::getUsername).collect(toSet());
        var profiles = candidates.stream().map(c -> toCoachProfile(c, usersMap)).toList();

        var aiRanked = aiClient.rankCoaches(
                request.goal(),
                request.focusAreas().stream().toList(),
                Optional.ofNullable(request.targetGroup()).orElse(StringUtils.EMPTY),
                profiles,
                topN
        ).stream()
                .filter(r -> candidateUsernames.contains(r.username()))
                .toList();

        var reasons = aiRanked.stream()
                .collect(toMap(AiClient.CoachRecommendation::username, AiClient.CoachRecommendation::reason, (a, b) -> a));
        var rankOrder = IntStream.range(0, aiRanked.size()).boxed()
                .collect(toMap(i -> aiRanked.get(i).username(), i -> i, (a, b) -> a));
        var rankedUsernames = aiRanked.stream().map(AiClient.CoachRecommendation::username).toList();

        return new AiRankingResult(reasons, rankOrder, rankedUsernames);
    }

    // Builds final pool: exact (trimmed if > target) + AI-recommended extras (filled if < target)
    public List<CoachPreviewDto> buildPool(List<Coach> exact, List<Coach> extras,
                                           AiRankingResult aiResult, Map<String, User> usersMap, int target) {
        var exactUsernames = exact.stream().map(Coach::getUsername).collect(toSet());
        var coachByUsername = Stream.concat(exact.stream(), extras.stream())
                .collect(toMap(Coach::getUsername, c -> c, (a, b) -> a));

        // Exact matches — trimmed by AI rank if over target
        var exactPool = exact.stream()
                .sorted(Comparator.comparing((Coach c) -> aiResult.rankOrder().getOrDefault(c.getUsername(), Integer.MAX_VALUE)))
                .limit(Math.min(exact.size(), target))
                .map(c -> toCoachPreviewDto(c, usersMap, aiResult.reasons().get(c.getUsername()), "exact"))
                .toList();

        // AI-recommended extras — only if exact didn't fill target, in AI rank order
        var recommendedPool = exact.size() >= target ? List.<CoachPreviewDto>of()
                : aiResult.rankedUsernames().stream()
                        .filter(u -> !exactUsernames.contains(u))
                        .filter(coachByUsername::containsKey)
                        .limit((long) target - exactPool.size())
                        .map(u -> toCoachPreviewDto(coachByUsername.get(u), usersMap, aiResult.reasons().get(u), "recommended"))
                        .toList();

        return Stream.concat(exactPool.stream(), recommendedPool.stream()).toList();
    }

    // Builds final response with counts, summary, and coaches sorted by AI relevance
    public CoachPreviewResponse toResponse(List<CoachPreviewDto> pool, Map<String, Integer> aiRankOrder) {
        var sorted = pool.stream()
                .sorted(Comparator.comparing((CoachPreviewDto d) -> aiRankOrder.getOrDefault(d.username(), Integer.MAX_VALUE))
                        .thenComparing(CoachPreviewDto::lastName))
                .toList();

        var exactCount = (int) sorted.stream().filter(d -> "exact".equals(d.matchType())).count();
        var recommendedCount = (int) sorted.stream().filter(d -> "recommended".equals(d.matchType())).count();
        var summary = recommendedCount == 0
                ? sorted.size() + " best-matched experts"
                : sorted.size() + " experts — " + exactCount + " exact + " + recommendedCount + " recommended";

        return new CoachPreviewResponse(sorted.size(), exactCount, recommendedCount, summary, sorted);
    }

    private AiClient.CoachProfile toCoachProfile(Coach c, Map<String, User> usersMap) {
        var u = Optional.ofNullable(usersMap.get(c.getUsername()));
        return new AiClient.CoachProfile(
                c.getUsername(),
                u.map(User::getFirstName).orElse(StringUtils.EMPTY),
                u.map(User::getLastName).orElse(StringUtils.EMPTY),
                c.getBio(),
                Optional.ofNullable(c.getPrimaryRoles()).map(Object::toString).orElse(StringUtils.EMPTY),
                Optional.ofNullable(c.getFields()).map(Object::toString).orElse(StringUtils.EMPTY),
                Optional.ofNullable(c.getTopics()).map(Object::toString).orElse(StringUtils.EMPTY),
                c.getPriority()
        );
    }

    private CoachPreviewDto toCoachPreviewDto(Coach c, Map<String, User> usersMap, String reason, String matchType) {
        var u = Optional.ofNullable(usersMap.get(c.getUsername()));
        return new CoachPreviewDto(
                c.getUsername(),
                u.map(User::getFirstName).orElse(StringUtils.EMPTY),
                u.map(User::getLastName).orElse(StringUtils.EMPTY),
                c.getBio(),
                new HashSet<>(c.getLanguagesList()),
                Optional.ofNullable(c.getFieldsList()).map(HashSet::new).map(Set.class::cast).orElse(Set.of()),
                reason,
                matchType
        );
    }
}
