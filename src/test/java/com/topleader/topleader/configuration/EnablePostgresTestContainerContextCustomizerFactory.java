/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.configuration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;
import lombok.EqualsAndHashCode;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.env.MapPropertySource;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextCustomizerFactory;
import org.springframework.test.context.MergedContextConfiguration;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;


/**
 * @author Daniel Slavik
 */
public class EnablePostgresTestContainerContextCustomizerFactory implements ContextCustomizerFactory {

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Inherited
    public @interface EnabledPostgresTestContainer {
    }

    @Override
    public ContextCustomizer createContextCustomizer(Class<?> testClass,
        List<ContextConfigurationAttributes> configAttributes) {
        if (!(AnnotatedElementUtils.hasAnnotation(testClass, EnabledPostgresTestContainer.class))) {
            return null;
        }
        return new PostgresTestContainerContextCustomizer();
    }

    @EqualsAndHashCode // See ContextCustomizer java doc
    private static class PostgresTestContainerContextCustomizer implements ContextCustomizer {

        private static final DockerImageName image = DockerImageName
            .parse("postgres")
            .withTag("15-alpine");

        @Override
        public void customizeContext(ConfigurableApplicationContext context, MergedContextConfiguration mergedConfig) {
            var externalUrl = System.getenv("TEST_DATASOURCE_URL");
            Map<String, Object> properties;

            if (externalUrl != null) {
                // External DB: native tests or CI (env var set)
                properties = Map.of(
                    "spring.datasource.url", externalUrl,
                    "spring.datasource.username", System.getenv().getOrDefault("TEST_DATASOURCE_USERNAME", "test"),
                    "spring.datasource.password", System.getenv().getOrDefault("TEST_DATASOURCE_PASSWORD", "test"),
                    "spring.datasource.driver-class-name", "org.postgresql.Driver",
                    "spring.test.database.replace", "NONE"
                );
            } else if (Boolean.getBoolean("spring.aot.processing")) {
                // AOT processing without external DB — should not happen in native-test flow,
                // but provide safe fallback that avoids starting Docker containers
                return;
            } else {
                // JVM tests: start TestContainers PostgreSQL
                var postgresContainer = new PostgreSQLContainer(image);
                postgresContainer.start();
                properties = Map.of(
                    "spring.datasource.url", postgresContainer.getJdbcUrl(),
                    "spring.datasource.username", postgresContainer.getUsername(),
                    "spring.datasource.password", postgresContainer.getPassword(),
                    "spring.datasource.driver-class-name", "org.postgresql.Driver",
                    "spring.test.database.replace", "NONE"
                );
            }

            var propertySource = new MapPropertySource("PostgresContainer Test Properties", properties);
            context.getEnvironment().getPropertySources().addFirst(propertySource);
        }

    }

}
