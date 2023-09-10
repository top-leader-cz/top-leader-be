/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
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
public class CoachImage {

    @Id
    private String username;

    private String type;

    @Lob
    @Column(length = 4000)
    private byte[] imageData;
}
