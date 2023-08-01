/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.history;

import com.topleader.topleader.history.data.StoredData;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;


/**
 * @author Daniel Slavik
 */
@Data
@Entity
@Accessors(chain = true)
public class DataHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String username;

    @Enumerated(EnumType.STRING)
    private Type type;

    private LocalDateTime createdAt;

    @Convert(converter = HistoryDataConverter.class)
    private StoredData data;

    public enum Type {
        STRENGTHS,
        VALUES
    }
}
