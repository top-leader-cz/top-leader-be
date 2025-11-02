/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach;

import ch.qos.logback.core.util.StringUtil;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Converter for CertificateType enum that handles empty strings from the database
 * by converting them to null instead of throwing an exception.
 */
@Converter
public class CertificateTypeConverter implements AttributeConverter<Coach.CertificateType, String> {

    @Override
    public String convertToDatabaseColumn(Coach.CertificateType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public Coach.CertificateType convertToEntityAttribute(String dbData) {
        if (StringUtils.isBlank(dbData)) {
            return null;
        }
        return Coach.CertificateType.valueOf(dbData);
    }
}