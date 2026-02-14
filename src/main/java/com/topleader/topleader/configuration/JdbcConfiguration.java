package com.topleader.topleader.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.topleader.topleader.common.ai.AiPrompt;
import com.topleader.topleader.common.notification.context.NotificationContext;
import com.topleader.topleader.common.util.common.JsonbValue;
import com.topleader.topleader.feedback.api.Summary;
import com.topleader.topleader.feedback.feedback_notification.FeedbackNotification;
import com.topleader.topleader.coach.Coach;
import com.topleader.topleader.common.calendar.domain.CalendarSyncInfo;
import com.topleader.topleader.common.notification.Notification;
import com.topleader.topleader.history.DataHistory;
import com.topleader.topleader.history.data.StoredData;
import com.topleader.topleader.session.scheduled_session.ScheduledSession;
import com.topleader.topleader.user.User;
import com.topleader.topleader.user.session.domain.UserArticle;
import org.apache.commons.lang3.StringUtils;
import org.postgresql.util.PGobject;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.core.dialect.JdbcPostgresDialect;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.topleader.topleader.common.util.common.JsonUtils.MAPPER;

@Configuration
public class JdbcConfiguration extends AbstractJdbcConfiguration {

    @Override
    public JdbcPostgresDialect jdbcDialect(@Lazy NamedParameterJdbcOperations operations) {
        return JdbcPostgresDialect.INSTANCE;
    }

    @Override
    public JdbcCustomConversions jdbcCustomConversions() {
        return new JdbcCustomConversions(List.of(
                new IntegerToBooleanConverter(),
                new PGobjectToStringConverter(),
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
                new FeedbackNotificationStatusWritingConverter(),
                new DataHistoryTypeReadingConverter(),
                new DataHistoryTypeWritingConverter(),
                new StoredDataReadingConverter(),
                new StoredDataWritingConverter(),
                new StringListFromPGobjectConverter(),
                new StringListFromStringConverter(),
                new StringListWritingConverter(),
                new StringSetFromPGobjectConverter(),
                new PrimaryRoleSetFromPGobjectConverter(),
                new UserStatusReadingConverter(),
                new UserStatusWritingConverter(),
                new StringToAuthoritiesReadingConverter(),
                new StringSetFromStringConverter(),
                new PrimaryRoleSetFromStringConverter(),
                new CalendarSyncInfoStatusReadingConverter(),
                new CalendarSyncInfoStatusWritingConverter(),
                new CalendarSyncInfoSyncTypeReadingConverter(),
                new CalendarSyncInfoSyncTypeWritingConverter(),
                new NotificationTypeReadingConverter(),
                new NotificationTypeWritingConverter(),
                new TimestampToZonedDateTimeConverter(),
                new ZonedDateTimeToTimestampConverter(),
                new TimestampToLocalDateTimeConverter(),
                new LocalDateTimeToTimestampConverter(),
                new TimeToLocalTimeConverter(),
                new LocalTimeToTimeConverter(),
                new UserArticleReadingConverter(),
                new UserArticleWritingConverter()
        ));
    }

    @ReadingConverter
    static class IntegerToBooleanConverter implements Converter<Integer, Boolean> {
        @Override
        public Boolean convert(Integer source) {
            return source != null && source != 0;
        }
    }

    @ReadingConverter
    static class PGobjectToStringConverter implements Converter<PGobject, String> {
        @Override
        public String convert(PGobject source) {
            return Optional.ofNullable(source)
                    .map(PGobject::getValue)
                    .orElse(null);
        }
    }

    private static <T> T readJson(String json, TypeReference<T> typeRef) {
        try {
            return MAPPER.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to convert from JSON", e);
        }
    }

    private static <T> T readJson(String json, Class<T> clazz) {
        try {
            return MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to convert from JSON", e);
        }
    }

    private static String writeJsonToString(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to convert to JSON", e);
        }
    }

    private static PGobject createJsonbPGobject(String jsonValue) {
        try {
            var pgObject = new PGobject();
            pgObject.setType("jsonb");
            pgObject.setValue(jsonValue);
            return pgObject;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to create JSONB PGobject", e);
        }
    }

    private static PGobject toPGobject(Object source) {
        return Optional.ofNullable(source)
                .map(obj -> createJsonbPGobject(writeJsonToString(obj)))
                .orElse(null);
    }

    @ReadingConverter
    static class AuthoritiesReadingConverter implements Converter<PGobject, Set<User.Authority>> {
        @Override
        public Set<User.Authority> convert(PGobject source) {
            return Optional.ofNullable(source)
                    .map(PGobject::getValue)
                    .filter(StringUtils::isNotBlank)
                    .map(v -> readJson(v, new TypeReference<Set<User.Authority>>() {}))
                    .orElse(Set.of(User.Authority.USER));
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
            return Optional.ofNullable(source)
                    .map(PGobject::getValue)
                    .map(JsonbValue::of)
                    .orElse(JsonbValue.ofNull());
        }
    }

    @WritingConverter
    static class JsonbValueWritingConverter implements Converter<JsonbValue, PGobject> {
        @Override
        public PGobject convert(JsonbValue source) {
            return Optional.ofNullable(source)
                    .filter(jsonbValue -> !jsonbValue.isNull())
                    .map(jsonbValue -> createJsonbPGobject(jsonbValue.json()))
                    .orElse(null);
        }
    }

    @ReadingConverter
    static class ScheduledSessionStatusReadingConverter implements Converter<String, ScheduledSession.Status> {
        @Override
        public ScheduledSession.Status convert(String source) {
            return Optional.ofNullable(source)
                    .filter(StringUtils::isNotBlank)
                    .map(ScheduledSession.Status::valueOf)
                    .orElse(null);
        }
    }

    @WritingConverter
    static class ScheduledSessionStatusWritingConverter implements Converter<ScheduledSession.Status, String> {
        @Override
        public String convert(ScheduledSession.Status source) {
            return Optional.ofNullable(source)
                    .map(Enum::name)
                    .orElse(null);
        }
    }

    @ReadingConverter
    static class NotificationContextReadingConverter implements Converter<String, NotificationContext> {
        @Override
        public NotificationContext convert(String source) {
            return Optional.ofNullable(source)
                    .filter(StringUtils::isNotBlank)
                    .map(json -> readJson(json, NotificationContext.class))
                    .orElse(null);
        }
    }

    @WritingConverter
    static class NotificationContextWritingConverter implements Converter<NotificationContext, String> {
        @Override
        public String convert(NotificationContext source) {
            return Optional.ofNullable(source)
                    .map(JdbcConfiguration::writeJsonToString)
                    .orElse(null);
        }
    }

    @ReadingConverter
    static class SummaryReadingConverter implements Converter<String, Summary> {
        @Override
        public Summary convert(String source) {
            return Optional.ofNullable(source)
                    .filter(StringUtils::isNotBlank)
                    .map(json -> readJson(json, Summary.class))
                    .orElse(null);
        }
    }

    @WritingConverter
    static class SummaryWritingConverter implements Converter<Summary, String> {
        @Override
        public String convert(Summary source) {
            return Optional.ofNullable(source)
                    .map(JdbcConfiguration::writeJsonToString)
                    .orElse(null);
        }
    }

    @ReadingConverter
    static class AiPromptTypeReadingConverter implements Converter<String, AiPrompt.PromptType> {
        @Override
        public AiPrompt.PromptType convert(String source) {
            return Optional.ofNullable(source)
                    .filter(StringUtils::isNotBlank)
                    .map(AiPrompt.PromptType::valueOf)
                    .orElse(null);
        }
    }

    @WritingConverter
    static class AiPromptTypeWritingConverter implements Converter<AiPrompt.PromptType, String> {
        @Override
        public String convert(AiPrompt.PromptType source) {
            return Optional.ofNullable(source)
                    .map(Enum::name)
                    .orElse(null);
        }
    }

    @ReadingConverter
    static class FeedbackNotificationStatusReadingConverter implements Converter<String, FeedbackNotification.Status> {
        @Override
        public FeedbackNotification.Status convert(String source) {
            return Optional.ofNullable(source)
                    .filter(StringUtils::isNotBlank)
                    .map(FeedbackNotification.Status::valueOf)
                    .orElse(null);
        }
    }

    @WritingConverter
    static class FeedbackNotificationStatusWritingConverter implements Converter<FeedbackNotification.Status, String> {
        @Override
        public String convert(FeedbackNotification.Status source) {
            return Optional.ofNullable(source)
                    .map(Enum::name)
                    .orElse(null);
        }
    }

    @ReadingConverter
    static class TimestampToZonedDateTimeConverter implements Converter<Timestamp, ZonedDateTime> {
        @Override
        public ZonedDateTime convert(Timestamp source) {
            return Optional.ofNullable(source)
                    .map(timestamp -> timestamp.toInstant().atZone(ZoneId.systemDefault()))
                    .orElse(null);
        }
    }

    @WritingConverter
    static class ZonedDateTimeToTimestampConverter implements Converter<ZonedDateTime, Timestamp> {
        @Override
        public Timestamp convert(ZonedDateTime source) {
            return Optional.ofNullable(source)
                    .map(zdt -> Timestamp.from(zdt.toInstant()))
                    .orElse(null);
        }
    }

    @ReadingConverter
    static class TimestampToLocalDateTimeConverter implements Converter<Timestamp, LocalDateTime> {
        @Override
        public LocalDateTime convert(Timestamp source) {
            return Optional.ofNullable(source)
                    .map(Timestamp::toLocalDateTime)
                    .orElse(null);
        }
    }

    @WritingConverter
    static class LocalDateTimeToTimestampConverter implements Converter<LocalDateTime, Timestamp> {
        @Override
        public Timestamp convert(LocalDateTime source) {
            return Optional.ofNullable(source)
                    .map(Timestamp::valueOf)
                    .orElse(null);
        }
    }

    @ReadingConverter
    static class StringListFromPGobjectConverter implements Converter<PGobject, List<String>> {
        @Override
        public List<String> convert(PGobject source) {
            return Optional.ofNullable(source)
                    .map(PGobject::getValue)
                    .filter(StringUtils::isNotBlank)
                    .map(v -> readJson(v, new TypeReference<List<String>>() {}))
                    .orElse(List.of());
        }
    }

    @ReadingConverter
    static class StringListFromStringConverter implements Converter<String, List<String>> {
        @Override
        public List<String> convert(String source) {
            return Optional.ofNullable(source)
                    .filter(StringUtils::isNotBlank)
                    .map(v -> readJson(v, new TypeReference<List<String>>() {}))
                    .orElse(List.of());
        }
    }

    @WritingConverter
    static class StringListWritingConverter implements Converter<List<String>, PGobject> {
        @Override
        public PGobject convert(List<String> source) {
            return toPGobject(source);
        }
    }

    @ReadingConverter
    static class DataHistoryTypeReadingConverter implements Converter<String, DataHistory.Type> {
        @Override
        public DataHistory.Type convert(String source) {
            return Optional.ofNullable(source)
                    .filter(StringUtils::isNotBlank)
                    .map(DataHistory.Type::valueOf)
                    .orElse(null);
        }
    }

    @WritingConverter
    static class DataHistoryTypeWritingConverter implements Converter<DataHistory.Type, String> {
        @Override
        public String convert(DataHistory.Type source) {
            return Optional.ofNullable(source)
                    .map(Enum::name)
                    .orElse(null);
        }
    }

    @ReadingConverter
    static class StoredDataReadingConverter implements Converter<String, StoredData> {
        @Override
        public StoredData convert(String source) {
            return Optional.ofNullable(source)
                    .filter(StringUtils::isNotBlank)
                    .map(json -> readJson(json, StoredData.class))
                    .orElse(null);
        }
    }

    @WritingConverter
    static class StoredDataWritingConverter implements Converter<StoredData, String> {
        @Override
        public String convert(StoredData source) {
            return Optional.ofNullable(source)
                    .map(JdbcConfiguration::writeJsonToString)
                    .orElse(null);
        }
    }

    @ReadingConverter
    static class StringSetFromPGobjectConverter implements Converter<PGobject, Set<String>> {
        @Override
        public Set<String> convert(PGobject source) {
            return Optional.ofNullable(source)
                    .map(PGobject::getValue)
                    .filter(StringUtils::isNotBlank)
                    .map(v -> readJson(v, new TypeReference<Set<String>>() {}))
                    .orElse(Set.of());
        }
    }

    @ReadingConverter
    static class PrimaryRoleSetFromPGobjectConverter implements Converter<PGobject, Set<Coach.PrimaryRole>> {
        @Override
        public Set<Coach.PrimaryRole> convert(PGobject source) {
            return Optional.ofNullable(source)
                    .map(PGobject::getValue)
                    .filter(StringUtils::isNotBlank)
                    .map(v -> readJson(v, new TypeReference<Set<Coach.PrimaryRole>>() {}))
                    .orElse(Set.of());
        }
    }

    @ReadingConverter
    static class UserStatusReadingConverter implements Converter<String, User.Status> {
        @Override
        public User.Status convert(String source) {
            return Optional.ofNullable(source)
                    .filter(StringUtils::isNotBlank)
                    .map(User.Status::valueOf)
                    .orElse(null);
        }
    }

    @WritingConverter
    static class UserStatusWritingConverter implements Converter<User.Status, String> {
        @Override
        public String convert(User.Status source) {
            return Optional.ofNullable(source)
                    .map(Enum::name)
                    .orElse(null);
        }
    }

    @ReadingConverter
    static class StringToAuthoritiesReadingConverter implements Converter<String, Set<User.Authority>> {
        @Override
        public Set<User.Authority> convert(String source) {
            return Optional.ofNullable(source)
                    .filter(StringUtils::isNotBlank)
                    .map(v -> readJson(v, new TypeReference<Set<User.Authority>>() {}))
                    .orElse(Set.of(User.Authority.USER));
        }
    }

    @ReadingConverter
    static class StringSetFromStringConverter implements Converter<String, Set<String>> {
        @Override
        public Set<String> convert(String source) {
            return Optional.ofNullable(source)
                    .filter(StringUtils::isNotBlank)
                    .map(v -> readJson(v, new TypeReference<Set<String>>() {}))
                    .orElse(Set.of());
        }
    }

    @ReadingConverter
    static class PrimaryRoleSetFromStringConverter implements Converter<String, Set<Coach.PrimaryRole>> {
        @Override
        public Set<Coach.PrimaryRole> convert(String source) {
            return Optional.ofNullable(source)
                    .filter(StringUtils::isNotBlank)
                    .map(v -> readJson(v, new TypeReference<Set<Coach.PrimaryRole>>() {}))
                    .orElse(Set.of());
        }
    }

    @ReadingConverter
    static class CalendarSyncInfoStatusReadingConverter implements Converter<String, CalendarSyncInfo.Status> {
        @Override
        public CalendarSyncInfo.Status convert(String source) {
            return Optional.ofNullable(source)
                    .filter(StringUtils::isNotBlank)
                    .map(CalendarSyncInfo.Status::valueOf)
                    .orElse(null);
        }
    }

    @WritingConverter
    static class CalendarSyncInfoStatusWritingConverter implements Converter<CalendarSyncInfo.Status, String> {
        @Override
        public String convert(CalendarSyncInfo.Status source) {
            return Optional.ofNullable(source)
                    .map(Enum::name)
                    .orElse(null);
        }
    }

    @ReadingConverter
    static class CalendarSyncInfoSyncTypeReadingConverter implements Converter<String, CalendarSyncInfo.SyncType> {
        @Override
        public CalendarSyncInfo.SyncType convert(String source) {
            return Optional.ofNullable(source)
                    .filter(StringUtils::isNotBlank)
                    .map(CalendarSyncInfo.SyncType::valueOf)
                    .orElse(null);
        }
    }

    @WritingConverter
    static class CalendarSyncInfoSyncTypeWritingConverter implements Converter<CalendarSyncInfo.SyncType, String> {
        @Override
        public String convert(CalendarSyncInfo.SyncType source) {
            return Optional.ofNullable(source)
                    .map(Enum::name)
                    .orElse(null);
        }
    }

    @ReadingConverter
    static class NotificationTypeReadingConverter implements Converter<String, Notification.Type> {
        @Override
        public Notification.Type convert(String source) {
            return Optional.ofNullable(source)
                    .filter(StringUtils::isNotBlank)
                    .map(Notification.Type::valueOf)
                    .orElse(null);
        }
    }

    @WritingConverter
    static class NotificationTypeWritingConverter implements Converter<Notification.Type, String> {
        @Override
        public String convert(Notification.Type source) {
            return Optional.ofNullable(source)
                    .map(Enum::name)
                    .orElse(null);
        }
    }

    // Native image: AOT-generated code can't convert java.sql.Time <-> java.time.LocalTime without explicit converters
    @ReadingConverter
    static class TimeToLocalTimeConverter implements Converter<Time, LocalTime> {
        @Override
        public LocalTime convert(Time source) {
            return Optional.ofNullable(source)
                    .map(Time::toLocalTime)
                    .orElse(null);
        }
    }

    @WritingConverter
    static class LocalTimeToTimeConverter implements Converter<LocalTime, Time> {
        @Override
        public Time convert(LocalTime source) {
            return Optional.ofNullable(source)
                    .map(Time::valueOf)
                    .orElse(null);
        }
    }

    @ReadingConverter
    static class UserArticleReadingConverter implements Converter<PGobject, UserArticle> {
        @Override
        public UserArticle convert(PGobject source) {
            return Optional.ofNullable(source)
                    .map(PGobject::getValue)
                    .filter(StringUtils::isNotBlank)
                    .map(json -> readJson(json, UserArticle.class))
                    .orElse(null);
        }
    }

    @WritingConverter
    static class UserArticleWritingConverter implements Converter<UserArticle, PGobject> {
        @Override
        public PGobject convert(UserArticle source) {
            return Optional.ofNullable(source)
                    .map(article -> createJsonbPGobject(writeJsonToString(article)))
                    .orElse(null);
        }
    }
}
