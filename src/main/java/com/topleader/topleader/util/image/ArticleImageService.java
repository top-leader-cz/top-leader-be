package com.topleader.topleader.util.image;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleImageService {

    private final RestClient restClient;

    @Value("${spring.ai.openai.api-key}")
    private String openaiApiKey;

    @Value("${openai.image.url}")
    private String imageGenerationUrl;


    public String generateImage(String imagePrompt) {
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
            .body(String.class);

        log.info("Received response from OpenAI: {}", response);
        return extractImageUrl(response);
    }

    private String extractImageUrl(String response) {
        try {
            int urlStart = response.indexOf("\"url\":\"") + 7;
            int urlEnd = response.indexOf("\"", urlStart);
            return response.substring(urlStart, urlEnd);
        } catch (Exception e) {
            log.error("Failed to extract image URL from response: {}", response, e);
            throw new RuntimeException("Failed to extract image URL from OpenAI response", e);
        }
    }


}