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


    public String generatePlaceholderImageData(String imagePrompt) {
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

        return svgContent;
    }


}