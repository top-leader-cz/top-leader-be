/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.history.data;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.experimental.Accessors;

import static com.topleader.topleader.history.data.StoredData.STRENGTH_TYPE;


/**
 * @author Daniel Slavik
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = StrengthStoredData.class, name = STRENGTH_TYPE),
})
@Data
@Accessors(chain = true)
public class StoredData {
    public static final String STRENGTH_TYPE = "STRENGTH_TYPE";
}
