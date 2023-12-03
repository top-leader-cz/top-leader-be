/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.history;

import com.topleader.topleader.history.data.StoredData;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;


/**
 * @author Daniel Slavik
 */
@Getter
@Setter
@ToString
@Entity
@Accessors(chain = true)
@NoArgsConstructor
public class DataHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "data_history_seq")
    @SequenceGenerator(name = "data_history_seq", sequenceName = "data_history_seq", allocationSize = 1)
    private Long id;

    private String username;

    @Enumerated(EnumType.STRING)
    private Type type;

    private LocalDateTime createdAt;

    @Convert(converter = HistoryDataConverter.class)
    @Column(columnDefinition = "text")
    private StoredData data;

    public enum Type {
        STRENGTHS,
        VALUES,
        USER_SESSION
    }
}
