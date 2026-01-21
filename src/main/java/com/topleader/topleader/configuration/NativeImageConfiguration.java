package com.topleader.topleader.configuration;

import com.topleader.topleader.common.ai.AiPrompt;
import com.topleader.topleader.common.calendar.calendly.CalendlyProperties;
import com.topleader.topleader.common.calendar.domain.CalendarSyncInfo;
import com.topleader.topleader.common.upload.UploadProperties;
import com.topleader.topleader.common.notification.Notification;
import com.topleader.topleader.common.notification.context.CoachLinkedNotificationContext;
import com.topleader.topleader.common.notification.context.CoachUnlinkedNotificationContext;
import com.topleader.topleader.common.notification.context.MessageNotificationContext;
import com.topleader.topleader.common.notification.context.NotificationContext;
import com.topleader.topleader.coach.Coach;
import com.topleader.topleader.feedback.api.Summary;
import com.topleader.topleader.feedback.feedback_notification.FeedbackNotification;
import com.topleader.topleader.history.DataHistory;
import com.topleader.topleader.history.data.StoredData;
import com.topleader.topleader.history.data.StrengthStoredData;
import com.topleader.topleader.history.data.UserSessionStoredData;
import com.topleader.topleader.history.data.ValuesStoredData;
import com.topleader.topleader.session.scheduled_session.ScheduledSession;
import com.topleader.topleader.user.User;
import com.topleader.topleader.user.session.domain.UserArticle;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

@Configuration
@ImportRuntimeHints(NativeImageConfiguration.TopLeaderRuntimeHints.class)
public class NativeImageConfiguration {

    static class TopLeaderRuntimeHints implements RuntimeHintsRegistrar {

        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            // Log4j2 TypeConverters - needed for native image
            registerLog4j2Classes(hints, classLoader);

            // Swagger UI / SpringDoc for native image
            // Note: Swagger UI resources only included when building with -Pswagger.ui=true (dev/qa)
            hints.resources().registerPattern("META-INF/resources/webjars/swagger-ui/**");
            hints.resources().registerPattern("org/springdoc/**");

            // SpringDoc classes (UI classes only registered if present on classpath)
            // API-only build (-Pswagger.ui=false) excludes UI classes automatically
            hints.reflection().registerTypeIfPresent(classLoader,
                    "org.springdoc.webmvc.api.OpenApiWebMvcResource",
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                    MemberCategory.INVOKE_DECLARED_METHODS,
                    MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerTypeIfPresent(classLoader,
                    "org.springdoc.webmvc.ui.SwaggerWelcomeWebMvc",
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                    MemberCategory.INVOKE_DECLARED_METHODS,
                    MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerTypeIfPresent(classLoader,
                    "org.springdoc.webmvc.ui.SwaggerConfigResource",
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                    MemberCategory.INVOKE_DECLARED_METHODS,
                    MemberCategory.INVOKE_PUBLIC_METHODS);

            // JSON polymorphic types - NotificationContext hierarchy
            registerForJsonSerialization(hints, NotificationContext.class);
            registerForJsonSerialization(hints, MessageNotificationContext.class);
            registerForJsonSerialization(hints, CoachUnlinkedNotificationContext.class);
            registerForJsonSerialization(hints, CoachLinkedNotificationContext.class);

            // JSON polymorphic types - StoredData hierarchy
            registerForJsonSerialization(hints, StoredData.class);
            registerForJsonSerialization(hints, StrengthStoredData.class);
            registerForJsonSerialization(hints, ValuesStoredData.class);
            registerForJsonSerialization(hints, UserSessionStoredData.class);
            registerForJsonSerialization(hints, UserSessionStoredData.ActionStepData.class);

            // DTOs and entities with JSON serialization
            registerForJsonSerialization(hints, Summary.class);
            registerForJsonSerialization(hints, UserArticle.class);

            // Configuration properties
            registerForJsonSerialization(hints, CalendlyProperties.class);
            registerForJsonSerialization(hints, UploadProperties.class);

            // Enums used in JDBC converters
            registerEnum(hints, User.Authority.class);
            registerEnum(hints, User.Status.class);
            registerEnum(hints, Coach.PrimaryRole.class);
            registerEnum(hints, ScheduledSession.Status.class);
            registerEnum(hints, FeedbackNotification.Status.class);
            registerEnum(hints, DataHistory.Type.class);
            registerEnum(hints, AiPrompt.PromptType.class);
            registerEnum(hints, CalendarSyncInfo.Status.class);
            registerEnum(hints, CalendarSyncInfo.SyncType.class);
            registerEnum(hints, Notification.Type.class);
        }

        private void registerForJsonSerialization(RuntimeHints hints, Class<?> clazz) {
            hints.reflection().registerType(clazz,
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                    MemberCategory.INVOKE_DECLARED_METHODS,
                    MemberCategory.INVOKE_PUBLIC_METHODS);
        }

        private void registerEnum(RuntimeHints hints, Class<?> enumClass) {
            hints.reflection().registerType(enumClass,
                    MemberCategory.INVOKE_PUBLIC_METHODS);
        }

        private void registerLog4j2Classes(RuntimeHints hints, ClassLoader classLoader) {
            // Log4j2 TypeConverters that need reflection
            hints.reflection().registerTypeIfPresent(classLoader,
                    "org.apache.logging.log4j.core.config.plugins.convert.TypeConverterRegistry",
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                    MemberCategory.INVOKE_DECLARED_METHODS);

            // Jetty security handler
            hints.reflection().registerTypeIfPresent(classLoader,
                    "org.eclipse.jetty.ee11.servlet.security.ConstraintSecurityHandler",
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                    MemberCategory.INVOKE_DECLARED_METHODS);
        }
    }
}
