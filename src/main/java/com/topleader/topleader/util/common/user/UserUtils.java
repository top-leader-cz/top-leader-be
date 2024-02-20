package com.topleader.topleader.util.common.user;

import com.topleader.topleader.user.User;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Optional;

import io.vavr.control.Try;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class UserUtils {

    public User fromEmail(String email) {
        var split = email.split("@");
        var names = split[0].split("\\.");
        var user = new User().setUsername(email.toLowerCase(Locale.ROOT));
        if(names.length == 2) {
            user.setFirstName(names[0]);
            user.setLastName(names[1]);
            return user;
        }
        user.setFirstName(names[0]);
        user.setLastName(names[0]);
        return user;
    }

    public static ZoneId getUserTimeZoneId(Optional<User> user) {
        return user.map(User::getTimeZone)
            .map(ZoneId::of)
            .orElse(ZoneOffset.UTC);
    }

    public  String localeToLanguage(String locale) {
        return Try.of(() -> Languages.valueOf(locale).language)
                .onFailure(e -> log.warn("Locale do not exist, defaulting to English. Locale: {}", locale))
                .getOrElse(Languages.en.language);
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
