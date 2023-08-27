/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.history.data;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.experimental.Accessors;

import static com.topleader.topleader.history.data.StoredData.STRENGTH_TYPE;
import static com.topleader.topleader.history.data.StoredData.USER_SESSION_TYPE;
import static com.topleader.topleader.history.data.StoredData.VALUES_TYPE;


/**
 * @author Daniel Slavik
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = StrengthStoredData.class, name = STRENGTH_TYPE),
    @JsonSubTypes.Type(value = ValuesStoredData.class, name = VALUES_TYPE),
    @JsonSubTypes.Type(value = UserSessionStoredData.class, name = USER_SESSION_TYPE),
})
@Data
@Accessors(chain = true)
public abstract class StoredData {

    public static final String STRENGTH_TYPE = "STRENGTH_TYPE";
    public static final String VALUES_TYPE = "VALUES_TYPE";
    public static final String USER_SESSION_TYPE = "USER_SESSION_TYPE";
}
