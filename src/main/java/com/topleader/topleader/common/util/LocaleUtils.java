package com.topleader.topleader.common.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;

@Slf4j
@UtilityClass
public class LocaleUtils {

    public static ZoneId zoneIdOrUtc(String timeZone) {
        return Optional.ofNullable(timeZone)
                .map(ZoneId::of)
                .orElse(ZoneOffset.UTC);
    }

    public String localeToLanguage(String locale) {
        try {
            return Languages.valueOf(locale).language;
        } catch (Exception e) {
            log.warn("Locale do not exist, defaulting to English. Locale: {}", locale);
            return Languages.en.language;
        }
    }

    public String defaultLocale() {
        return Languages.en.name();
    }

    public String defaultLanguage() {
        return Languages.en.language;
    }

    public enum Languages {
        fr("French"),
        cs("Czech"),
        de("German"),
        en("English");

        public final String language;

        Languages(String language) {
            this.language = language;
        }
    }
}
