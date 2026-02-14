package com.topleader.topleader.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.security.jackson2.SecurityJackson2Modules;

@Slf4j
@Configuration
public class SessionSerializationConfig {

    @Bean("springSessionConversionService")
    public ConversionService springSessionConversionService() {
        var mapper = sessionObjectMapper();
        var service = new GenericConversionService();
        service.addConverter(Object.class, byte[].class, new JacksonSerializer(mapper));
        service.addConverter(byte[].class, Object.class, new JacksonDeserializer(mapper));
        return service;
    }

    private ObjectMapper sessionObjectMapper() {
        var mapper = new ObjectMapper();
        mapper.registerModules(SecurityJackson2Modules.getModules(getClass().getClassLoader()));
        SecurityJackson2Modules.enableDefaultTyping(mapper);
        return mapper;
    }

    static class JacksonSerializer implements Converter<Object, byte[]> {
        private final ObjectMapper mapper;

        JacksonSerializer(ObjectMapper mapper) {
            this.mapper = mapper;
        }

        @Override
        public byte[] convert(Object source) {
            try {
                return mapper.writeValueAsBytes(source);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to serialize session attribute to JSON", e);
            }
        }
    }

    @Slf4j
    static class JacksonDeserializer implements Converter<byte[], Object> {
        private final ObjectMapper mapper;

        JacksonDeserializer(ObjectMapper mapper) {
            this.mapper = mapper;
        }

        @Override
        public Object convert(byte[] source) {
            try {
                return mapper.readValue(source, Object.class);
            } catch (Exception e) {
                log.warn("Failed to deserialize session attribute — old Java-serialized session will be invalidated", e);
                return null;
            }
        }
    }
}
