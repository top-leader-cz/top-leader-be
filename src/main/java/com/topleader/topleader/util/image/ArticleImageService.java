package com.topleader.topleader.util.image;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleImageService {

    private final RestClient restClient;

    @Value("${spring.ai.openai.api-key}")
    private String openaiApiKey;

    @Value("${openai.image.url}")
    private String imageGenerationUrl;

    public String generateImageFromPrompt(String imagePrompt) {
        log.info("Generating image from prompt: {}", imagePrompt);
        
        return Try.of(() -> generateImageWithDallE(imagePrompt))
                .onFailure(e -> log.error("Failed to generate image with DALL-E for prompt: [{}]", imagePrompt, e))
                .getOrElse(() -> generateFallbackImage(imagePrompt));
    }

    public byte[] downloadAndGenerateImage(String imagePrompt) {
        log.info("Generating and downloading image from prompt: {}", imagePrompt);
        
        return Try.of(() -> {
            String imageUrl = generateImageWithDallE(imagePrompt);
            return downloadImageFromUrl(imageUrl);
        })
        .onFailure(e -> log.error("Failed to generate and download image with DALL-E for prompt: [{}]", imagePrompt, e))
        .getOrElse(() -> {
            try {
                String fallbackUrl = generateFallbackImage(imagePrompt);
                return downloadImageFromUrl(fallbackUrl);
            } catch (Exception ex) {
                log.error("Failed to download fallback image", ex);
                return generatePlaceholderImageData(imagePrompt);
            }
        });
    }

    @SneakyThrows
    public byte[] downloadImageFromUrl(String imageUrl) {
        log.info("Downloading image from URL: {}", imageUrl);
        try {
            return restClient.get()
                    .uri(new URI(imageUrl))
                    .retrieve()
                    .body(byte[].class);
        } catch (Exception e) {
            log.error("Failed to download image from URL: {}", imageUrl, e);
            throw new RuntimeException("Failed to download image", e);
        }
    }

    public byte[] generatePlaceholderImageData(String imagePrompt) {
        String safePrompt = imagePrompt == null ? "placeholder" : imagePrompt;
        String displayText = safePrompt.length() > 30 ? safePrompt.substring(0, 30) + "..." : safePrompt;
        
        String svgContent = String.format(
            "<svg width=\"400\" height=\"300\" xmlns=\"http://www.w3.org/2000/svg\">" +
            "<rect width=\"100%%\" height=\"100%%\" fill=\"#2C3E50\"/>" +
            "<text x=\"50%%\" y=\"50%%\" font-family=\"Arial\" font-size=\"16\" fill=\"white\" text-anchor=\"middle\" dominant-baseline=\"middle\">" +
            "Image: %s" +
            "</text></svg>", 
            displayText
        );
        
        return svgContent.getBytes();
    }

    private String generateImageWithDallE(String imagePrompt) {
        var requestBody = Map.of(
                "model", "dall-e-3",
                "prompt", optimizePromptForDallE(imagePrompt),
                "n", 1,
                "size", "1024x1024",
                "quality", "standard",
                "style", "natural"
        );

        var response = restClient.post()
                .uri(imageGenerationUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + openaiApiKey)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(requestBody)
                .retrieve()
                .body(DallEResponse.class);

        if (response != null && response.data() != null && !response.data().isEmpty()) {
            var imageUrl = response.data().getFirst().url();
            log.info("Successfully generated DALL-E image: {}", imageUrl);
            return imageUrl;
        }

        throw new RuntimeException("Failed to generate image - empty response from DALL-E");
    }

    private String optimizePromptForDallE(String originalPrompt) {
        if (originalPrompt.length() > 1000) {
            originalPrompt = originalPrompt.substring(0, 997) + "...";
        }
        
        log.info("Optimized prompt: {}", originalPrompt);
        return originalPrompt;
    }

    private String generateFallbackImage(String imagePrompt) {
        log.info("Using fallback image generation for prompt: {}", imagePrompt);

        // Try Unsplash API first as a fallback
        return Try.of(() -> generateUnsplashImage(imagePrompt))
                .onFailure(e -> log.warn("Unsplash fallback failed: {}", e.getMessage()))
                .getOrElse(() -> generatePlaceholderImage(imagePrompt));
    }

    private String generateUnsplashImage(String imagePrompt) {
        // Extract keywords from the prompt for Unsplash search
        var keywords = extractKeywords(imagePrompt);
        var searchTerm = String.join(",", keywords);
        
        // Use Unsplash Source API for free, random images
        var unsplashUrl = "https://source.unsplash.com/1200x800/?" + searchTerm;
        log.info("Generated Unsplash image URL: {}", unsplashUrl);
        return unsplashUrl;
    }

    private List<String> extractKeywords(String prompt) {
        if (prompt == null || prompt.trim().isEmpty()) {
            return List.of("business", "leadership", "professional");
        }
        
        // Simple keyword extraction - in production this could be more sophisticated
        var keywords = Stream.of(
                prompt.toLowerCase()
                        .replaceAll("[^a-zA-Z0-9\\s]", "")
                        .split("\\s+"))
                .filter(word -> word.length() > 3)
                .filter(word -> !isStopWord(word))
                .limit(3)
                .toList();
        
        // If no valid keywords were extracted, return default ones
        if (keywords.isEmpty()) {
            return List.of("business", "leadership", "professional");
        }
        
        return keywords;
    }

    private boolean isStopWord(String word) {
        var stopWords = List.of("the", "and", "for", "with", "this", "that", "from", "they", "have", "been");
        return stopWords.contains(word.toLowerCase());
    }

    private String generatePlaceholderImage(String imagePrompt) {
        // Last resort: placeholder image
        String safePrompt = imagePrompt == null ? "placeholder" : imagePrompt;
        var encodedPrompt = safePrompt.replaceAll("[^a-zA-Z0-9\\s]", "")
                .replaceAll("\\s+", "%20")
                .toLowerCase();
        
        var displayText = encodedPrompt.length() > 30 ? 
                encodedPrompt.substring(0, 30) + "..." : encodedPrompt;
        
        return String.format("https://via.placeholder.com/1200x800/2C3E50/FFFFFF?text=%s", 
                displayText);
    }

    public record DallEResponse(List<ImageData> data) {}
    
    public record ImageData(String url, @JsonProperty("revised_prompt") String revisedPrompt) {}
}