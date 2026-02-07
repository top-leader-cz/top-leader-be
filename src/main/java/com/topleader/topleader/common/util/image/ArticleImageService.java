package com.topleader.topleader.common.util.image;

import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleImageService {

    private static final int MAX_PROMPT_LENGTH = 100;

    private final RestClient restClient;

    @Autowired(required = false)
    private GcsLightweightClient gcsClient;

    private final RetryPolicy<Object> retryPolicy;

    @Value("${spring.ai.openai.api-key}")
    private String openaiApiKey;

    @Value("${openai.image.url}")
    private String imageGenerationUrl;

    @Value("${gcp.storage.bucket-name:}")
    private String bucketName;

    public String generateImage(String imagePrompt) {
        try {
           return Failsafe.with(retryPolicy).get(() -> {
                var response = generate(imagePrompt);
                var imageUrl = extractImageUrl(response);
                var revisedPrompt = extractRevisedPrompt(response);
                var image = downloadImage(imageUrl);
                return storeImageInGcp(image, imagePrompt, revisedPrompt);
            });

        } catch (Exception e) {
            log.error("Failed to generate or store image for prompt: {}", imagePrompt, e);
            return null;
        }

    }

    private DaliResponse generate(String imagePrompt) {
        log.info("Generating image with DALL-E 3 for prompt: {}", imagePrompt);

        var requestBody = """
            {
                "model": "dall-e-3",
                "prompt": "%s",
                "n": 1,
                "size": "1024x1024",
                "quality": "standard",
                "response_format": "url"
            }
            """.formatted(imagePrompt);

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

    private String extractRevisedPrompt(DaliResponse response) {
        return  response.data().stream()
                .map(DaliResponse.ImageData::revisedPrompt)
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
    private String storeImageInGcp(byte[] image, String prompt, String revisedPrompt) {
        try {
            if (image == null || gcsClient == null) {
                log.warn("No image data or GCS client unavailable for prompt: {}", prompt);
                return null;
            }

            var fileName = generateFileName(prompt);
            var metadata = Map.of(
                    "prompt", prompt,
                    "generated_at", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    "model", "dall-e-3",
                    "size", "1024x1024",
                    "searchable_keywords", extractKeywords(revisedPrompt)
            );

            var gcpUrl = gcsClient.uploadImage(image, fileName, metadata);
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

    private String extractKeywords(String prompt) {
        return prompt.replaceAll("[^a-zA-Z0-9\\s]", "")
            .replaceAll("\\s+", " ")
            .toLowerCase()
            .trim()
            .replaceAll("\\s", ",");
    }

    public String getImageAsBase64(String gcpUrl) {
        try {
            if (gcsClient == null || StringUtils.isBlank(gcpUrl) || !gcpUrl.startsWith("gs://")) {
                log.warn("GCS client unavailable or invalid GCP URL: {}", gcpUrl);
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
