package com.topleader.topleader.common.util.image;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleImageService {

    private static final int MAX_PROMPT_LENGTH = 100;
    private static final int MIN_MATCHES = 2;
    private static final List<String> STEM_SUFFIXES = List.of(
            "ation", "tion", "ment", "ness", "ling", "ally", "ing", "ive", "ous", "ful",
            "ity", "ble", "ant", "ent", "ism", "ist", "ate", "ize", "ise", "ial", "ual",
            "ion", "age", "ly", "ed", "er", "al", "es"
    );
    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "the", "of", "in", "with", "and", "for", "to", "on", "at", "by",
            "is", "it", "or", "as", "be", "that", "this", "from", "are", "was", "has",
            "style", "illustration", "realistic", "modern", "clean", "concept", "scene",
            "showing", "flat", "editorial", "muted", "tones", "colors", "warm", "soft"
    );

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Autowired(required = false)
    private GcsLightweightClient gcsClient;

    private final RetryPolicy<Object> retryPolicy;

    private volatile Set<String> cachedImageNames = new HashSet<>();

    @Value("${spring.ai.openai.api-key}")
    private String openaiApiKey;

    @Value("${openai.image.url}")
    private String imageGenerationUrl;

    @Value("${gcp.storage.bucket-name:}")
    private String bucketName;

    @EventListener(ApplicationReadyEvent.class)
    public void preloadImageCache() {
        if (gcsClient == null || StringUtils.isBlank(bucketName)) {
            return;
        }
        Thread.ofVirtual().start(() -> {
            try {
                Failsafe.with(RetryPolicy.builder()
                                .withMaxRetries(3)
                                .withDelay(Duration.ofSeconds(1))
                                .handle(Exception.class)
                                .onRetry(e -> log.warn("Retrying GCS image preload (attempt {})", e.getAttemptCount()))
                                .build())
                        .run(() -> {
                            cachedImageNames = new HashSet<>(gcsClient.listObjects());
                            log.info("Pre-loaded {} image names from GCS bucket", cachedImageNames.size());
                        });
            } catch (Exception e) {
                log.error("Failed to pre-load GCS image names after retries, will load on first request", e);
            }
        });
    }

    public String generateImage(String imagePrompt) {
        try {
            var existingMatch = findReusableImage(imagePrompt);
            if (existingMatch != null) {
                log.info("[IMAGE-CACHE] Reused existing image for prompt '{}': {}", imagePrompt, existingMatch);
                return existingMatch;
            }

            return Failsafe.with(retryPolicy).get(() -> {
                var response = generate(imagePrompt);
                var imageUrl = extractImageUrl(response);
                var image = downloadImage(imageUrl);
                var gcpUrl = storeImageInGcp(image, imagePrompt);
                if (gcpUrl != null) {
                    addToCache(generateFileName(imagePrompt));
                    log.info("[IMAGE-GENERATED] New image generated via DALL-E for prompt '{}': {}", imagePrompt, gcpUrl);
                }
                return gcpUrl;
            });

        } catch (Exception e) {
            log.error("Failed to generate or store image for prompt: {}", imagePrompt, e);
            return null;
        }
    }

    private String findReusableImage(String imagePrompt) {
        if (gcsClient == null || StringUtils.isBlank(bucketName)) {
            return null;
        }

        try {
            var imageNames = getOrLoadImageNames();
            if (imageNames.isEmpty()) {
                return null;
            }

            var promptKeywords = extractKeywords(imagePrompt);
            if (promptKeywords.isEmpty()) {
                return null;
            }

            return imageNames.stream()
                    .map(fileName -> {
                        var fileKeywords = extractKeywordsFromFileName(fileName);
                        var matchCount = countFuzzyMatches(promptKeywords, fileKeywords);
                        return Map.entry(fileName, matchCount);
                    })
                    .filter(e -> e.getValue() >= MIN_MATCHES)
                    .max(Map.Entry.comparingByValue())
                    .map(e -> {
                        log.info("[IMAGE-MATCH] Matched '{}' -> '{}' (matches: {})", imagePrompt, e.getKey(), e.getValue());
                        return String.format("gs://%s/%s", bucketName, e.getKey());
                    })
                    .orElse(null);
        } catch (Exception e) {
            log.warn("Failed to search for reusable images, will generate new one", e);
        }
        return null;
    }

    private Set<String> extractKeywords(String text) {
        return Arrays.stream(text.toLowerCase().replaceAll("[^a-z0-9\\s]", "").split("\\s+"))
                .filter(w -> w.length() > 2)
                .filter(w -> !STOP_WORDS.contains(w))
                .map(ArticleImageService::stem)
                .collect(Collectors.toSet());
    }

    private Set<String> extractKeywordsFromFileName(String fileName) {
        var name = fileName.replace(".png", "");
        return Arrays.stream(name.split("_"))
                .filter(w -> w.length() > 2)
                .filter(w -> !STOP_WORDS.contains(w))
                .map(ArticleImageService::stem)
                .collect(Collectors.toSet());
    }

    private long countFuzzyMatches(Set<String> promptWords, Set<String> fileWords) {
        return promptWords.stream()
                .filter(wordA -> fileWords.stream().anyMatch(wordB -> fuzzyMatch(wordA, wordB)))
                .count();
    }

    private static boolean fuzzyMatch(String a, String b) {
        if (a.equals(b)) {
            return true;
        }
        var shorter = a.length() <= b.length() ? a : b;
        var longer = a.length() > b.length() ? a : b;
        return shorter.length() >= 4 && longer.startsWith(shorter);
    }

    static String stem(String word) {
        if (word.length() <= 4) {
            return word;
        }
        for (var suffix : STEM_SUFFIXES) {
            if (word.endsWith(suffix) && word.length() - suffix.length() >= 3) {
                return word.substring(0, word.length() - suffix.length());
            }
        }
        return word;
    }

    private Set<String> getOrLoadImageNames() throws Exception {
        if (cachedImageNames.isEmpty()) {
            synchronized (this) {
                if (cachedImageNames.isEmpty()) {
                    cachedImageNames = new HashSet<>(gcsClient.listObjects());
                    log.info("Loaded {} image names from GCS bucket", cachedImageNames.size());
                }
            }
        }
        return cachedImageNames;
    }

    private void addToCache(String fileName) {
        cachedImageNames.add(fileName);
    }

    private DaliResponse generate(String imagePrompt) {
        log.info("Generating image with DALL-E 3 for prompt: {}", imagePrompt);

        var requestBody = objectMapper.createObjectNode()
                .put("model", "dall-e-3")
                .put("prompt", imagePrompt)
                .put("n", 1)
                .put("size", "1024x1024")
                .put("quality", "standard")
                .put("response_format", "url")
                .toString();

        var response = restClient.post()
                .uri(imageGenerationUrl)
                .header("Authorization", "Bearer " + openaiApiKey)
                .header("Content-Type", "application/json")
                .body(requestBody)
                .retrieve()
                .body(DaliResponse.class);

        log.info("Received response from OpenAI: {}", response);
        return response;
    }

    private String extractImageUrl(DaliResponse response) {
        return response.data().stream()
                .map(DaliResponse.ImageData::url)
                .findFirst()
                .orElseThrow();
    }

    private byte[] downloadImage(String imageUrl) {
        try {
            log.info("Downloading image from OpenAI URL: {}", imageUrl);

           return restClient.get()
                    .uri(URI.create(imageUrl))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .header("Accept", "image/*,*/*;q=0.8")
                    .header("Accept-Language", "en-US,en;q=0.5")
                    .header("Connection", "keep-alive")
                    .retrieve()
                    .body(byte[].class);

        } catch (Exception e) {
            log.error("Failed to download image from OpenAI", e);
            return null;
        }
    }
    private String storeImageInGcp(byte[] image, String prompt) {
        try {
            if (image == null || gcsClient == null) {
                log.warn("No image data or GCS client unavailable for prompt: {}", prompt);
                return null;
            }

            var fileName = generateFileName(prompt);
            var gcpUrl = gcsClient.uploadImage(image, fileName);
            log.info("Image stored in GCP bucket with URL: {}", gcpUrl);
            return gcpUrl;
        } catch (Exception e) {
            log.error("Failed to store image in GCP bucket", e);
            return null;
        }
    }

    private String generateFileName(String prompt) {
        var sanitizedPrompt = prompt.replaceAll("[^a-zA-Z0-9\\s-]", "")
                .replaceAll("\\s+", "_")
                .toLowerCase();

        if (MAX_PROMPT_LENGTH < sanitizedPrompt.length()) {
            sanitizedPrompt = sanitizedPrompt.substring(0, MAX_PROMPT_LENGTH);
        }

        return String.format("%s.png", sanitizedPrompt);
    }

    public String getImageAsBase64(String gcpUrl) {
        try {
            if (gcsClient == null || StringUtils.isBlank(gcpUrl) || !gcpUrl.startsWith("gs://")) {
              log.debug("GCS client unavailable or invalid GCP URL: {}", gcpUrl);
                return null;
            }

            var content = gcsClient.downloadImage(gcpUrl);

            if (content == null || content.length == 0) {
                log.warn("No content found for GCP URL: {}", gcpUrl);
                return null;
            }

            var base64Content = Base64.getEncoder().encodeToString(content);
            return "data:image/png;base64," + base64Content;

        } catch (Exception e) {
            log.error("Failed to retrieve image from GCP URL: {}", gcpUrl, e);
            return null;
        }
    }
}
