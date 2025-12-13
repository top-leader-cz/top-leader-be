package com.topleader.topleader.util.image;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import io.github.resilience4j.retry.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
@Lazy
public class ArticleImageService {

    private static final int MAX_PROMPT_LENGTH = 100;

    private final RestClient restClient;

    private final Storage storage;

    private final Retry retry;

    @Value("${spring.ai.openai.api-key}")
    private String openaiApiKey;

    @Value("${openai.image.url}")
    private String imageGenerationUrl;

    @Value("${gcp.storage.bucket-name}")
    private String bucketName;

    public String generateImage(String imagePrompt) {
        try {
           return retry.executeSupplier(() -> {
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
            if (image == null) {
                log.warn("No image data to store for prompt: {}", prompt);
                return null;
            }

            var fileName = generateFileName(prompt);
            var blobId = BlobId.of(bucketName, fileName);

            var blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType("image/png")
                .setMetadata(java.util.Map.of(
                    "prompt", prompt,
                    "generated_at", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    "model", "dall-e-3",
                    "size", "1024x1024",
                    "searchable_keywords", extractKeywords(revisedPrompt)
                ))
                .build();

            storage.create(blobInfo, image);
            String gcpUrl = String.format("gs://%s/%s", bucketName, fileName);
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
            if (StringUtils.isBlank(gcpUrl) || !gcpUrl.startsWith("gs://")) {
                log.warn("Invalid GCP URL: {}", gcpUrl);
                return null;
            }

            var parts = gcpUrl.replace("gs://", "").split("/", 2);
            if (parts.length != 2) {
                log.error("Invalid GCP URL format: {}", gcpUrl);
                return null;
            }

            var bucketName = parts[0];
            var fileName = parts[1];

            var blobId = BlobId.of(bucketName, fileName);
            var content = storage.readAllBytes(blobId);

            if (content == null) {
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