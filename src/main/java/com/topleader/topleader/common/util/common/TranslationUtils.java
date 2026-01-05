package com.topleader.topleader.common.util.common;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@UtilityClass
public class TranslationUtils {

    public Map<String, String> getTranslation() {
        var translations =  JsonUtils.fromJsonString(
                FileUtils.loadFileAsString("translation/questions-translation.json"),
                new  TypeReference<List<Translation>>() {});
        return translations.stream()
                .collect(Collectors.toMap(Translation::getKey, Translation::getValue));

    }

    public String translate(String key, Map<String, String> translations) {
        return translations.getOrDefault(key, key);
    }
}
