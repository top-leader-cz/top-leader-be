/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.entity.history;

import com.topleader.topleader.entity.converter.HistoryDataConverter;
import com.topleader.topleader.entity.history.data.StoredData;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

    private Type type;

    @Convert(converter = HistoryDataConverter.class)
    private StoredData data;

    public enum Type {
        STRENGTHS
    }
}
