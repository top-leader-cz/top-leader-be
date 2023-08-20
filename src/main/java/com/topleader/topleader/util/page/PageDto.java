/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.util.page;

import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;


/**
 * @author Daniel Slavik
 */
public record PageDto(Integer pageNumber, Integer pageSize) {
    public Pageable toPageable() {
        return PageRequest.of(
            Optional.ofNullable(pageNumber).orElse(0),
            Optional.ofNullable(pageSize).orElse(10)
        );
    }
}
