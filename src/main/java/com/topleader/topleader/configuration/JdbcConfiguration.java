/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.topleader.topleader.coach.Coach;
import com.topleader.topleader.user.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;

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
                new StringSetReadingConverter(),
                new StringSetWritingConverter(),
                new PrimaryRolesReadingConverter(),
                new PrimaryRolesWritingConverter()
        ));
    }

    @ReadingConverter
    static class AuthoritiesReadingConverter implements Converter<String, Set<User.Authority>> {
        @Override
        public Set<User.Authority> convert(String source) {
            if (source == null || source.isBlank()) {
                return Set.of(User.Authority.USER);
            }
            try {
                return MAPPER.readValue(source, new TypeReference<>() {});
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Failed to convert authorities from JSON", e);
            }
        }
    }

    @WritingConverter
    static class AuthoritiesWritingConverter implements Converter<Set<User.Authority>, String> {
        @Override
        public String convert(Set<User.Authority> source) {
            if (source == null) {
                return null;
            }
            try {
                return MAPPER.writeValueAsString(source);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Failed to convert authorities to JSON", e);
            }
        }
    }

    @ReadingConverter
    static class StringSetReadingConverter implements Converter<String, Set<String>> {
        @Override
        public Set<String> convert(String source) {
            if (source == null || source.isBlank()) {
                return Set.of();
            }
            try {
                return MAPPER.readValue(source, new TypeReference<>() {});
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Failed to convert string set from JSON", e);
            }
        }
    }

    @WritingConverter
    static class StringSetWritingConverter implements Converter<Set<String>, String> {
        @Override
        public String convert(Set<String> source) {
            if (source == null) {
                return null;
            }
            try {
                return MAPPER.writeValueAsString(source);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Failed to convert string set to JSON", e);
            }
        }
    }

    @ReadingConverter
    static class PrimaryRolesReadingConverter implements Converter<String, Set<Coach.PrimaryRole>> {
        @Override
        public Set<Coach.PrimaryRole> convert(String source) {
            if (source == null || source.isBlank()) {
                return Set.of();
            }
            try {
                return MAPPER.readValue(source, new TypeReference<>() {});
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Failed to convert primary roles from JSON", e);
            }
        }
    }

    @WritingConverter
    static class PrimaryRolesWritingConverter implements Converter<Set<Coach.PrimaryRole>, String> {
        @Override
        public String convert(Set<Coach.PrimaryRole> source) {
            if (source == null) {
                return null;
            }
            try {
                return MAPPER.writeValueAsString(source);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Failed to convert primary roles to JSON", e);
            }
        }
    }
}
