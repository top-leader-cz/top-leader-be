/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.history;

import com.topleader.topleader.history.data.StoredData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import com.topleader.topleader.common.entity.BaseEntity;

@Data
@EqualsAndHashCode(callSuper=false)
@Table("data_history")
@Accessors(chain = true)
public class DataHistory extends BaseEntity {
    private String username;

    private Type type;

    private LocalDateTime createdAt;

    private StoredData data;

    public enum Type {
        STRENGTHS,
        VALUES,
        USER_SESSION
    }
}
