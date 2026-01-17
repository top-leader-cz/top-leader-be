/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.history;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.topleader.topleader.history.data.StoredData;
import com.topleader.topleader.common.util.common.JsonUtils;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;


/**
 * @author Daniel Slavik
 */
@Getter
@Setter
@ToString
@Table("data_history")
@Accessors(chain = true)
@NoArgsConstructor
public class DataHistory {

    @Id
    private Long id;

    private String username;

    private Type type;

    private LocalDateTime createdAt;

    @JsonRawValue
    private String data;

    public StoredData getDataObject() {
        return data != null ? JsonUtils.fromJsonString(data, StoredData.class) : null;
    }

    public DataHistory setData(StoredData storedData) {
        this.data = storedData != null ? JsonUtils.toJsonString(storedData) : null;
        return this;
    }

    public enum Type {
        STRENGTHS,
        VALUES,
        USER_SESSION
    }
}
