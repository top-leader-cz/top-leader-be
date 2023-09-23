/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach.client;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
public class CoachClientView {

    @Id
    private String id;

    private String coach;

    private String client;

    private String clientFirstName;

    private String clientLastName;

    private LocalDateTime lastSession;

    private LocalDateTime nextSession;
}
