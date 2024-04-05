/*
 * Copyright (c) 2024 Price f(x), s.r.o.
 */
package com.topleader.topleader.google;

import jakarta.persistence.Entity;
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
public class SyncEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sync_event_id_seq")
    @SequenceGenerator(name = "sync_event_id_seq", sequenceName = "sync_event_id_seq", allocationSize = 1)
    private Long id;

    private String username;

    private String externalId;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

}
