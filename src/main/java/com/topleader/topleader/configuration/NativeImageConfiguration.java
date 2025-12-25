package com.topleader.topleader.configuration;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

/**
 * GraalVM Native Image configuration providing runtime hints for reflection,
 * resources, and serialization needed by libraries that use dynamic features.
 */
@Configuration
@ImportRuntimeHints(NativeImageConfiguration.TopLeaderRuntimeHints.class)
public class NativeImageConfiguration {

    static class TopLeaderRuntimeHints implements RuntimeHintsRegistrar {

        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            registerGoogleApiHints(hints);
            registerIcal4jHints(hints);
            registerVelocityHints(hints);
            registerVavrHints(hints);
            registerResourceHints(hints);
        }

        private void registerGoogleApiHints(RuntimeHints hints) {
            // Google Calendar API models
            registerReflectionForClass(hints, "com.google.api.services.calendar.Calendar");
            registerReflectionForClass(hints, "com.google.api.services.calendar.Calendar$Events");
            registerReflectionForClass(hints, "com.google.api.services.calendar.Calendar$Events$List");
            registerReflectionForClass(hints, "com.google.api.services.calendar.Calendar$Events$Insert");
            registerReflectionForClass(hints, "com.google.api.services.calendar.Calendar$Events$Delete");
            registerReflectionForClass(hints, "com.google.api.services.calendar.Calendar$FreeBusy");
            registerReflectionForClass(hints, "com.google.api.services.calendar.model.Event");
            registerReflectionForClass(hints, "com.google.api.services.calendar.model.EventDateTime");
            registerReflectionForClass(hints, "com.google.api.services.calendar.model.EventAttendee");
            registerReflectionForClass(hints, "com.google.api.services.calendar.model.Events");
            registerReflectionForClass(hints, "com.google.api.services.calendar.model.FreeBusyRequest");
            registerReflectionForClass(hints, "com.google.api.services.calendar.model.FreeBusyResponse");
            registerReflectionForClass(hints, "com.google.api.services.calendar.model.FreeBusyRequestItem");
            registerReflectionForClass(hints, "com.google.api.services.calendar.model.FreeBusyCalendar");
            registerReflectionForClass(hints, "com.google.api.services.calendar.model.TimePeriod");

            // Google OAuth2 API
            registerReflectionForClass(hints, "com.google.api.services.oauth2.Oauth2");
            registerReflectionForClass(hints, "com.google.api.services.oauth2.model.Userinfo");

            // Google API Client core
            registerReflectionForClass(hints, "com.google.api.client.googleapis.auth.oauth2.GoogleCredential");
            registerReflectionForClass(hints, "com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse");
            registerReflectionForClass(hints, "com.google.api.client.http.GenericUrl");
            registerReflectionForClass(hints, "com.google.api.client.json.gson.GsonFactory");
            registerReflectionForClass(hints, "com.google.api.client.json.jackson2.JacksonFactory");

            // Cloud SQL Socket Factory
            registerReflectionForClass(hints, "com.google.cloud.sql.postgres.SocketFactory");
        }

        private void registerIcal4jHints(RuntimeHints hints) {
            // iCal4j core types
            registerReflectionForClass(hints, "net.fortuna.ical4j.model.Calendar");
            registerReflectionForClass(hints, "net.fortuna.ical4j.model.component.VEvent");
            registerReflectionForClass(hints, "net.fortuna.ical4j.model.component.VTimeZone");
            registerReflectionForClass(hints, "net.fortuna.ical4j.model.TimeZone");
            registerReflectionForClass(hints, "net.fortuna.ical4j.model.TimeZoneRegistryFactory");
            registerReflectionForClass(hints, "net.fortuna.ical4j.model.DefaultTimeZoneRegistryFactory");

            // iCal4j properties
            registerReflectionForClass(hints, "net.fortuna.ical4j.model.property.Method");
            registerReflectionForClass(hints, "net.fortuna.ical4j.model.property.Uid");
            registerReflectionForClass(hints, "net.fortuna.ical4j.model.property.Organizer");
            registerReflectionForClass(hints, "net.fortuna.ical4j.model.property.Attendee");
            registerReflectionForClass(hints, "net.fortuna.ical4j.model.property.Description");
            registerReflectionForClass(hints, "net.fortuna.ical4j.model.property.Priority");
            registerReflectionForClass(hints, "net.fortuna.ical4j.model.property.Clazz");
            registerReflectionForClass(hints, "net.fortuna.ical4j.model.property.Status");
            registerReflectionForClass(hints, "net.fortuna.ical4j.model.property.Sequence");
            registerReflectionForClass(hints, "net.fortuna.ical4j.model.property.Transp");
            registerReflectionForClass(hints, "net.fortuna.ical4j.model.property.DtStart");
            registerReflectionForClass(hints, "net.fortuna.ical4j.model.property.DtEnd");
            registerReflectionForClass(hints, "net.fortuna.ical4j.model.property.Summary");
            registerReflectionForClass(hints, "net.fortuna.ical4j.model.property.Location");

            // iCal4j parameters
            registerReflectionForClass(hints, "net.fortuna.ical4j.model.parameter.Cn");
            registerReflectionForClass(hints, "net.fortuna.ical4j.model.parameter.Role");
            registerReflectionForClass(hints, "net.fortuna.ical4j.model.parameter.CuType");
            registerReflectionForClass(hints, "net.fortuna.ical4j.model.parameter.PartStat");
            registerReflectionForClass(hints, "net.fortuna.ical4j.model.parameter.Rsvp");

            // iCal4j resources
            hints.resources().registerPattern("net/fortuna/ical4j/**");
            hints.resources().registerPattern("zoneinfo/**");
            hints.resources().registerPattern("zoneinfo-global/**");
        }

        private void registerVelocityHints(RuntimeHints hints) {
            registerReflectionForClass(hints, "org.apache.velocity.app.VelocityEngine");
            registerReflectionForClass(hints, "org.apache.velocity.VelocityContext");
            registerReflectionForClass(hints, "org.apache.velocity.Template");
            registerReflectionForClass(hints, "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
            registerReflectionForClass(hints, "org.apache.velocity.runtime.RuntimeInstance");
        }

        private void registerVavrHints(RuntimeHints hints) {
            // Vavr core types
            registerReflectionForClass(hints, "io.vavr.control.Option");
            registerReflectionForClass(hints, "io.vavr.control.Option$Some");
            registerReflectionForClass(hints, "io.vavr.control.Option$None");
            registerReflectionForClass(hints, "io.vavr.control.Either");
            registerReflectionForClass(hints, "io.vavr.control.Either$Left");
            registerReflectionForClass(hints, "io.vavr.control.Either$Right");
            registerReflectionForClass(hints, "io.vavr.control.Try");
            registerReflectionForClass(hints, "io.vavr.control.Try$Success");
            registerReflectionForClass(hints, "io.vavr.control.Try$Failure");

            // Vavr tuples
            registerReflectionForClass(hints, "io.vavr.Tuple");
            registerReflectionForClass(hints, "io.vavr.Tuple0");
            registerReflectionForClass(hints, "io.vavr.Tuple1");
            registerReflectionForClass(hints, "io.vavr.Tuple2");
            registerReflectionForClass(hints, "io.vavr.Tuple3");
            registerReflectionForClass(hints, "io.vavr.Tuple4");
            registerReflectionForClass(hints, "io.vavr.Tuple5");

            // Vavr collections
            registerReflectionForClass(hints, "io.vavr.collection.List");
            registerReflectionForClass(hints, "io.vavr.collection.Stream");
            registerReflectionForClass(hints, "io.vavr.collection.Map");
            registerReflectionForClass(hints, "io.vavr.collection.HashMap");
            registerReflectionForClass(hints, "io.vavr.collection.Set");
            registerReflectionForClass(hints, "io.vavr.collection.HashSet");
        }

        private void registerResourceHints(RuntimeHints hints) {
            // Velocity templates
            hints.resources().registerPattern("templates/**/*.vm");

            // Flyway migrations
            hints.resources().registerPattern("db/migration/**/*.sql");

            // Application configuration
            hints.resources().registerPattern("application*.yml");
            hints.resources().registerPattern("application*.yaml");
            hints.resources().registerPattern("application*.properties");

            // META-INF services
            hints.resources().registerPattern("META-INF/services/**");
        }

        private void registerReflectionForClass(RuntimeHints hints, String className) {
            try {
                Class<?> clazz = Class.forName(className);
                hints.reflection().registerType(clazz,
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.INVOKE_DECLARED_METHODS,
                        MemberCategory.DECLARED_FIELDS);
            } catch (ClassNotFoundException e) {
                // Class not available, skip registration
            }
        }
    }
}
