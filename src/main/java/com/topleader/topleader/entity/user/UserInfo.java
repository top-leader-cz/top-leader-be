/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.entity.user;

import com.topleader.topleader.entity.converter.SetConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.util.Set;
import lombok.Data;
import lombok.experimental.Accessors;


/**
 * @author Daniel Slavik
 */
@Data
@Entity
@Accessors(chain = true)
public class UserInfo {

    @Id
    private String username;

    @Convert(converter = SetConverter.class)
    private Set<String> strengths;

    @Convert(converter = SetConverter.class)
    private Set<String> values;

    @Convert(converter = SetConverter.class)
    private Set<String> areaOfDevelopment;

    private String notes;
}
