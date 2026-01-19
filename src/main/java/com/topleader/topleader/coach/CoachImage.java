/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.data.relational.core.mapping.Table;
import com.topleader.topleader.common.entity.BaseEntity;


/**
 * @author Daniel Slavik
 */
@Getter
@Setter
@ToString
@Table("coach_image")
@Accessors(chain = true)
@NoArgsConstructor
public class CoachImage extends BaseEntity {
    private String username;

    private String type;

    private byte[] imageData;
}
