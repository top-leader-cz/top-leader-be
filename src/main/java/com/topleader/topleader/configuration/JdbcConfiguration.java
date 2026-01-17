/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.topleader.topleader.common.ai.AiPrompt;
import com.topleader.topleader.common.notification.context.NotificationContext;
import com.topleader.topleader.common.util.common.JsonbValue;
import com.topleader.topleader.feedback.api.Summary;
import com.topleader.topleader.feedback.feedback_notification.FeedbackNotification;
import com.topleader.topleader.session.scheduled_session.ScheduledSession;
import com.topleader.topleader.user.User;
import org.apache.commons.lang3.StringUtils;
import org.postgresql.util.PGobject;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import static com.topleader.topleader.common.util.common.JsonUtils.MAPPER;

@Configuration
public class JdbcConfiguration extends AbstractJdbcConfiguration {

    @Override
    public JdbcCustomConversions jdbcCustomConversions() {
        return new JdbcCustomConversions(List.of(
                new AuthoritiesReadingConverter(),
                new AuthoritiesWritingConverter(),
                new JsonbValueReadingConverter(),
                new JsonbValueWritingConverter(),
                new ScheduledSessionStatusReadingConverter(),
                new ScheduledSessionStatusWritingConverter(),
                new NotificationContextReadingConverter(),
                new NotificationContextWritingConverter(),
                new SummaryReadingConverter(),
                new SummaryWritingConverter(),
                new AiPromptTypeReadingConverter(),
                new AiPromptTypeWritingConverter(),
                new FeedbackNotificationStatusReadingConverter(),
                new FeedbackNotificationStatusWritingConverter()
        ));
    }

    @ReadingConverter
    static class AuthoritiesReadingConverter implements Converter<PGobject, Set<User.Authority>> {
        @Override
        public Set<User.Authority> convert(PGobject source) {
            var value = source != null ? source.getValue() : null;
            if (StringUtils.isBlank(value)) {
                return Set.of(User.Authority.USER);
            }
            try {
                return MAPPER.readValue(value, new TypeReference<>() {});
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Failed to convert authorities from JSON", e);
            }
        }
    }

    @WritingConverter
    static class AuthoritiesWritingConverter implements Converter<Set<User.Authority>, PGobject> {
        @Override
        public PGobject convert(Set<User.Authority> source) {
            return toPGobject(source);
        }
    }

    @ReadingConverter
    static class JsonbValueReadingConverter implements Converter<PGobject, JsonbValue> {
        @Override
        public JsonbValue convert(PGobject source) {
            if (source == null) {
                return JsonbValue.ofNull();
            }
            return JsonbValue.of(source.getValue());
        }
    }

    @WritingConverter
    static class JsonbValueWritingConverter implements Converter<JsonbValue, PGobject> {
        @Override
        public PGobject convert(JsonbValue source) {
            if (source == null || source.isNull()) {
                return null;
            }
            try {
                var pgObject = new PGobject();
                pgObject.setType("jsonb");
                pgObject.setValue(source.json());
                return pgObject;
            } catch (SQLException e) {
                throw new IllegalStateException("Failed to convert JsonbValue to PGobject", e);
            }
        }
    }

    private static PGobject toPGobject(Object source) {
        if (source == null) {
            return null;
        }
        try {
            var pgObject = new PGobject();
            pgObject.setType("jsonb");
            pgObject.setValue(MAPPER.writeValueAsString(source));
            return pgObject;
        } catch (JsonProcessingException | SQLException e) {
            throw new IllegalStateException("Failed to convert to JSONB", e);
        }
    }


    @ReadingConverter
    static class ScheduledSessionStatusReadingConverter implements Converter<String, ScheduledSession.Status> {
        @Override
        public ScheduledSession.Status convert(String source) {
            if (StringUtils.isBlank(source)) {
                return null;
            }
            return ScheduledSession.Status.valueOf(source);
        }
    }

    @WritingConverter
    static class ScheduledSessionStatusWritingConverter implements Converter<ScheduledSession.Status, String> {
        @Override
        public String convert(ScheduledSession.Status source) {
            return source != null ? source.name() : null;
        }
    }

    @ReadingConverter
    static class NotificationContextReadingConverter implements Converter<String, NotificationContext> {
        @Override
        public NotificationContext convert(String source) {
            if (StringUtils.isBlank(source)) {
                return null;
            }
            try {
                return MAPPER.readValue(source, NotificationContext.class);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Failed to convert NotificationContext from JSON", e);
            }
        }
    }

    @WritingConverter
    static class NotificationContextWritingConverter implements Converter<NotificationContext, String> {
        @Override
        public String convert(NotificationContext source) {
            if (source == null) {
                return null;
            }
            try {
                return MAPPER.writeValueAsString(source);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Failed to convert NotificationContext to JSON", e);
            }
        }
    }

    @ReadingConverter
    static class SummaryReadingConverter implements Converter<String, Summary> {
        @Override
        public Summary convert(String source) {
            if (StringUtils.isBlank(source)) {
                return null;
            }
            try {
                return MAPPER.readValue(source, Summary.class);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Failed to convert Summary from JSON", e);
            }
        }
    }

    @WritingConverter
    static class SummaryWritingConverter implements Converter<Summary, String> {
        @Override
        public String convert(Summary source) {
            if (source == null) {
                return null;
            }
            try {
                return MAPPER.writeValueAsString(source);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Failed to convert Summary to JSON", e);
            }
        }
    }

    @ReadingConverter
    static class AiPromptTypeReadingConverter implements Converter<String, AiPrompt.PromptType> {
        @Override
        public AiPrompt.PromptType convert(String source) {
            if (StringUtils.isBlank(source)) {
                return null;
            }
            return AiPrompt.PromptType.valueOf(source);
        }
    }

    @WritingConverter
    static class AiPromptTypeWritingConverter implements Converter<AiPrompt.PromptType, String> {
        @Override
        public String convert(AiPrompt.PromptType source) {
            return source != null ? source.name() : null;
        }
    }

    @ReadingConverter
    static class FeedbackNotificationStatusReadingConverter implements Converter<String, FeedbackNotification.Status> {
        @Override
        public FeedbackNotification.Status convert(String source) {
            if (StringUtils.isBlank(source)) {
                return null;
            }
            return FeedbackNotification.Status.valueOf(source);
        }
    }

    @WritingConverter
    static class FeedbackNotificationStatusWritingConverter implements Converter<FeedbackNotification.Status, String> {
        @Override
        public String convert(FeedbackNotification.Status source) {
            return source != null ? source.name() : null;
        }
    }
}
