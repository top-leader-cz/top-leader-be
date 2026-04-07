package com.topleader.topleader.program.recommendation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.topleader.topleader.common.ai.AiClient;
import com.topleader.topleader.common.ai.UserArticle;
import com.topleader.topleader.common.ai.UserPreview;
import com.topleader.topleader.program.participant.ProgramParticipant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProgramRecommendationService {

    private final AiClient aiClient;
    private final FocusAreaLabelService focusAreaLabelService;
    private final ProgramParticipantRecommendationRepository recommendationRepository;
    private final ObjectMapper objectMapper;

    @Async
    public void generateAsync(ProgramParticipant participant, String language) {
        try {
            var focusAreaEnglish = focusAreaLabelService.englishLabel(participant.getFocusArea());
            log.info("Generating program recommendations, participant: [{}], focus: [{}]",
                    participant.getId(), focusAreaEnglish);

            var articles = Optional.ofNullable(aiClient.generateProgramArticles(
                    participant.getUsername(), focusAreaEnglish, participant.getPersonalGoal(), language))
                    .orElseGet(List::of);
            var videos = Optional.ofNullable(aiClient.generateProgramPreviews(
                    participant.getUsername(), focusAreaEnglish, participant.getPersonalGoal()))
                    .orElseGet(List::of);

            replaceRecommendations(participant, articles, videos);
            log.info("Generated {} articles and {} videos for participant [{}]",
                    articles.size(), videos.size(), participant.getId());
        } catch (Exception e) {
            log.warn("Failed to generate program recommendations for participant [{}]: {}",
                    participant.getId(), e.getMessage(), e);
        }
    }

    public LearnMoreDto loadForParticipant(ProgramParticipant participant, String language) {
        var focusAreaLabel = focusAreaLabelService.labelFor(participant.getFocusArea(), language);
        var rows = recommendationRepository.findByParticipantAndCycle(
                participant.getId(), participant.getCurrentCycle());

        var articles = rows.stream()
                .filter(r -> r.getType() == ProgramParticipantRecommendation.Type.ARTICLE)
                .map(r -> toArticleDto(r.getContent()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        var videos = rows.stream()
                .filter(r -> r.getType() == ProgramParticipantRecommendation.Type.VIDEO)
                .map(r -> toVideoDto(r.getContent()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        return new LearnMoreDto(focusAreaLabel, articles, videos);
    }

    private void replaceRecommendations(
            ProgramParticipant participant, List<UserArticle> articles, List<UserPreview> videos) {
        recommendationRepository.deleteByParticipantAndCycle(
                participant.getId(), participant.getCurrentCycle());

        var now = LocalDateTime.now();
        var entities = Stream.concat(
                IntStream.range(0, articles.size())
                        .mapToObj(i -> buildEntity(
                                participant, ProgramParticipantRecommendation.Type.ARTICLE,
                                serialize(articles.get(i)), i + 1, now)),
                IntStream.range(0, videos.size())
                        .mapToObj(i -> buildEntity(
                                participant, ProgramParticipantRecommendation.Type.VIDEO,
                                serialize(videos.get(i)), i + 1, now))
        )
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        if (!entities.isEmpty()) {
            recommendationRepository.saveAll(entities);
        }
    }

    private Optional<ProgramParticipantRecommendation> buildEntity(
            ProgramParticipant participant,
            ProgramParticipantRecommendation.Type type,
            Optional<String> content,
            int rank,
            LocalDateTime now) {
        return content.map(json -> new ProgramParticipantRecommendation()
                .setProgramParticipantId(participant.getId())
                .setCycle(participant.getCurrentCycle())
                .setType(type)
                .setContent(json)
                .setRelevanceRank(rank)
                .setCreatedAt(now));
    }

    private Optional<String> serialize(Object value) {
        try {
            return Optional.of(objectMapper.writeValueAsString(value));
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize recommendation content: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<RecommendationDto> toArticleDto(String json) {
        return deserialize(json, UserArticle.class)
                .map(a -> new RecommendationDto(
                        ProgramParticipantRecommendation.Type.ARTICLE.name(),
                        a.getTitle(),
                        a.getUrl(),
                        Optional.ofNullable(a.getReadTime()).filter(StringUtils::isNotBlank).orElse(StringUtils.EMPTY),
                        null));
    }

    private Optional<RecommendationDto> toVideoDto(String json) {
        return deserialize(json, UserPreview.class)
                .map(v -> new RecommendationDto(
                        ProgramParticipantRecommendation.Type.VIDEO.name(),
                        v.getTitle(),
                        v.getUrl(),
                        Optional.ofNullable(v.getLength()).filter(StringUtils::isNotBlank).orElse(StringUtils.EMPTY),
                        v.getThumbnail()));
    }

    private <T> Optional<T> deserialize(String json, Class<T> type) {
        try {
            return Optional.ofNullable(objectMapper.readValue(json, type));
        } catch (JsonProcessingException e) {
            log.warn("Failed to deserialize recommendation content: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
