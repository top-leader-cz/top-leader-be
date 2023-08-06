/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import java.time.LocalDate;
import java.util.Set;
import lombok.Data;
import lombok.experimental.Accessors;


/**
 * @author Daniel Slavik
 */
@Data
@Entity
@Accessors(chain = true)
public class Coach {

    @Id
    private String username;

    private Boolean publicProfile;

    private String firstName;

    private String lastName;

    private String email;

    @Lob
    @Column(length = 1000)
    private byte[] photo;

    @Column(length = 1000)
    private String bio;

    private String timeZone;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> languages;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> fields;

    private LocalDate experienceSince;

    private String rate;
}
