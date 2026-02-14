package com.topleader.topleader.configuration;

import com.topleader.topleader.admin.AdminViewController;
import com.topleader.topleader.common.ai.AiPrompt;
import com.topleader.topleader.common.ai.McpToolsConfig;
import com.topleader.topleader.common.calendar.calendly.CalendlyProperties;
import com.topleader.topleader.common.calendar.calendly.domain.TokenResponse;
import com.topleader.topleader.common.calendar.domain.CalendarSyncInfo;
import com.topleader.topleader.common.calendar.google.GoogleCalendarApiClientFactory;
import com.topleader.topleader.common.upload.UploadProperties;
import com.topleader.topleader.common.notification.Notification;
import com.topleader.topleader.common.notification.context.CoachLinkedNotificationContext;
import com.topleader.topleader.common.notification.context.CoachUnlinkedNotificationContext;
import com.topleader.topleader.common.notification.context.MessageNotificationContext;
import com.topleader.topleader.common.notification.context.NotificationContext;
import com.topleader.topleader.common.util.common.Translation;
import com.topleader.topleader.common.util.image.DaliResponse;
import com.topleader.topleader.common.util.image.GcsLightweightClient;
import com.topleader.topleader.coach.Coach;
import com.topleader.topleader.feedback.api.Summary;
import com.topleader.topleader.feedback.feedback_notification.FeedbackNotification;
import com.topleader.topleader.history.DataHistory;
import com.topleader.topleader.history.data.StoredData;
import com.topleader.topleader.history.data.StrengthStoredData;
import com.topleader.topleader.history.data.UserSessionStoredData;
import com.topleader.topleader.history.data.ValuesStoredData;
import com.topleader.topleader.feedback.api.QuestionType;
import com.topleader.topleader.session.coaching_package.CoachingPackage;
import com.topleader.topleader.session.scheduled_session.ScheduledSession;
import com.topleader.topleader.session.user_allocation.UserAllocation;
import com.topleader.topleader.user.User;
import com.topleader.topleader.user.badge.Badge;
import com.topleader.topleader.user.session.domain.RecommendedGrowth;
import com.topleader.topleader.user.session.domain.UserArticle;
import com.topleader.topleader.user.session.domain.UserPreview;
import com.topleader.topleader.user.session.reminder.SessionReminderView;
import com.topleader.topleader.user.token.Token;
import java.util.stream.Stream;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
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

             // Note: Swagger UI resources only included when building with -Pswagger.ui=true (dev/qa)
            hints.resources().registerPattern("META-INF/resources/webjars/swagger-ui/**");
            hints.resources().registerPattern("org/springdoc/**");
            hints.resources().registerPattern("db/migration/**");
            hints.resources().registerPattern("translation/**");

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
            registerForJsonSerialization(hints, UserPreview.class);
            registerForJsonSerialization(hints, RecommendedGrowth.class);
            registerForJsonSerialization(hints, Translation.class);

            // Records bound from query parameters (not @RequestBody — Spring AOT doesn't auto-register these)
            registerForJsonSerialization(hints, AdminViewController.FilterDto.class);

            // RestClient response types (Jackson deserialization via reflection)
            registerForJsonSerialization(hints, GcsLightweightClient.MetadataTokenResponse.class);
            registerForJsonSerialization(hints, GcsLightweightClient.GcsListResponse.class);
            registerForJsonSerialization(hints, GcsLightweightClient.GcsObject.class);
            registerForJsonSerialization(hints, GoogleCalendarApiClientFactory.TokenResponse.class);
            registerForJsonSerialization(hints, GoogleCalendarApiClientFactory.FreeBusyResponse.class);
            registerForJsonSerialization(hints, GoogleCalendarApiClientFactory.FreeBusyResponse.CalendarEntry.class);
            registerForJsonSerialization(hints, GoogleCalendarApiClientFactory.FreeBusyResponse.BusySlot.class);
            registerForJsonSerialization(hints, GoogleCalendarApiClientFactory.FreeBusyResponse.ErrorEntry.class);
            registerForJsonSerialization(hints, TokenResponse.class);
            registerForJsonSerialization(hints, DaliResponse.class);
            registerForJsonSerialization(hints, DaliResponse.ImageData.class);
            registerForJsonSerialization(hints, tools.jackson.databind.JsonNode.class);

            // MCP Tool request/response types for Spring AI
            registerForJsonSerialization(hints, McpToolsConfig.UserProfileRequest.class);
            registerForJsonSerialization(hints, McpToolsConfig.UserProfileResponse.class);
            registerForJsonSerialization(hints, McpToolsConfig.SessionHistoryEntry.class);
            registerForJsonSerialization(hints, McpToolsConfig.CoachSearchRequest.class);
            registerForJsonSerialization(hints, McpToolsConfig.CoachByNameRequest.class);
            registerForJsonSerialization(hints, McpToolsConfig.CoachResponse.class);

            // Configuration properties
            registerForJsonSerialization(hints, CalendlyProperties.class);
            registerForJsonSerialization(hints, UploadProperties.class);

            // Spring Security session types (Java serialization via Spring Session JDBC)
            registerSessionSecurityClasses(hints, classLoader);

            // Flyway extension classes (reflectively accessed during migration)
            hints.reflection().registerTypeIfPresent(classLoader,
                    "org.flywaydb.core.internal.configuration.extensions.PrepareScriptFilenameConfigurationExtension",
                    MemberCategory.INVOKE_DECLARED_METHODS,
                    MemberCategory.INVOKE_PUBLIC_METHODS);

            // Enums used in JDBC (default enum conversion uses Enum.valueOf() = reflection)
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
            registerEnum(hints, CoachingPackage.PoolType.class);
            registerEnum(hints, CoachingPackage.PackageStatus.class);
            registerEnum(hints, UserAllocation.AllocationStatus.class);
            registerEnum(hints, Token.Type.class);
            registerEnum(hints, Badge.AchievementType.class);
            registerEnum(hints, QuestionType.class);
            registerEnum(hints, SessionReminderView.ReminderInterval.class);
        }

        private void registerForJsonSerialization(RuntimeHints hints, Class<?> clazz) {
            hints.reflection().registerType(clazz,
                    MemberCategory.ACCESS_DECLARED_FIELDS,
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                    MemberCategory.INVOKE_DECLARED_METHODS,
                    MemberCategory.INVOKE_PUBLIC_METHODS);
        }

        private void registerEnum(RuntimeHints hints, Class<?> enumClass) {
            hints.reflection().registerType(enumClass,
                    MemberCategory.INVOKE_PUBLIC_METHODS);
        }

        private void registerSessionSecurityClasses(RuntimeHints hints, ClassLoader classLoader) {
            // Security types stored in HTTP session (default Java serialization)
            Stream.of(
                    "org.springframework.security.core.context.SecurityContextImpl",
                    "org.springframework.security.authentication.UsernamePasswordAuthenticationToken",
                    "org.springframework.security.core.authority.SimpleGrantedAuthority",
                    "org.springframework.security.core.userdetails.User",
                    "org.springframework.security.web.authentication.WebAuthenticationDetails",
                    "java.util.Collections$UnmodifiableRandomAccessList",
                    "java.util.Collections$UnmodifiableSet",
                    "java.util.Collections$UnmodifiableMap",
                    "java.util.ArrayList",
                    "java.util.HashSet"
            ).forEach(className -> {
                try {
                    hints.serialization().registerType(TypeReference.of(Class.forName(className)));
                } catch (ClassNotFoundException e) {
                    // class not on classpath — skip
                }
            });
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
