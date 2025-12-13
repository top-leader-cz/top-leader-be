/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.history;

import com.topleader.topleader.history.data.StoredData;
import com.topleader.topleader.util.common.JsonUtils;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Optional;



/**
 * @author Daniel Slavik
 */
@Converter
public class HistoryDataConverter implements AttributeConverter<StoredData, String> {

    @Override
    public String convertToDatabaseColumn(StoredData data) {
        return Optional.ofNullable(data)
            .map(JsonUtils::toJsonString)
            .orElse(null);
    }

    @Override
    public StoredData convertToEntityAttribute(String dbData) {
        return Optional.ofNullable(dbData)
            .map(d -> JsonUtils.fromJsonString(d, StoredData.class))
            .orElse(null);
    }
}

