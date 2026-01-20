/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach.client;

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
@Table("coach_client_view")
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
