package com.topleader.topleader.program.recommendation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class FocusAreaLabelService {

    private static final String RESOURCE = "focus-areas.json";
    private static final String DEFAULT_LANGUAGE = "en";

    private final Map<String, Map<String, String>> labels;

    public FocusAreaLabelService(ObjectMapper objectMapper) {
        try (var is = new ClassPathResource(RESOURCE).getInputStream()) {
            this.labels = objectMapper.readValue(is, new TypeReference<>() {});
            log.info("Loaded {} focus area labels from {}", labels.size(), RESOURCE);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + RESOURCE, e);
        }
    }

    public String labelFor(String key, String language) {
        return Optional.ofNullable(labels.get(key))
                .map(translations -> Optional.ofNullable(translations.get(language))
                        .orElseGet(() -> translations.get(DEFAULT_LANGUAGE)))
                .orElse(key);
    }

    public String englishLabel(String key) {
        return labelFor(key, DEFAULT_LANGUAGE);
    }
}
