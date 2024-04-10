package com.topleader.topleader.user.session;

import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@UtilityClass
public class SessionUtils {

    private Map<String, String> DEVELOPMENT_MAPPING = Map.of(
            "1", "Become an active listener",
            "2", "Become more efficient",
            "3", "Show appreciation, recognition and empathy for your team",
            "4", "Be honest, transparent and accountable",
            "5", "Be an effective communicator",
            "6", "Being more assertive",
            "7", "Negotiate effectively",
            "8", "Be more self-confident",
            "9", "Apply critical thinking"
    );

    public String getDevelopment(String key) {
        return DEVELOPMENT_MAPPING.getOrDefault(key, "Unknown development area");
    }

    public List<String> getDevelopments(List<String> keys) {
        return keys.stream()
                .map(SessionUtils::getDevelopment)
                .collect(Collectors.toList());
    }
}

