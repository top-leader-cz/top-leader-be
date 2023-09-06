/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.notification.context;

import com.topleader.topleader.util.ObjectMapperUtils;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Optional;

import static com.topleader.topleader.util.ObjectMapperUtils.fromJsonString;


/**
 * @author Daniel Slavik
 */
@Converter
public class NotificationContextConverter implements AttributeConverter<NotificationContext, String> {

    @Override
    public String convertToDatabaseColumn(NotificationContext data) {
        return Optional.ofNullable(data)
            .map(ObjectMapperUtils::toJsonString)
            .orElse(null);
    }

    @Override
    public NotificationContext convertToEntityAttribute(String dbData) {
        return Optional.ofNullable(dbData)
            .map(d -> fromJsonString(d, NotificationContext.class))
            .orElse(null);
    }
}
