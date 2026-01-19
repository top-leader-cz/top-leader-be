/*
 * Copyright (c) 2024 Price f(x), s.r.o.
 */
package com.topleader.topleader.common.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;


/**
 * Base entity class for all entities with auto-generated Long ID.
 *
 * Implements Persistable to control INSERT vs UPDATE behavior in Spring Data JDBC:
 * - isNew() returns true when ID is null → Spring Data JDBC performs INSERT without ID field
 * - isNew() returns false when ID is set → Spring Data JDBC performs UPDATE
 *
 * The database DEFAULT (e.g., nextval('sequence_name')) generates the ID during INSERT.
 *
 * Note: @Accessors(chain = true) is intentionally NOT used on BaseEntity to avoid
 * Lombok type inference issues with method chaining in subclasses.
 * Each subclass should declare @Accessors(chain = true) for their own fields.
 */
@Getter
@Setter
public abstract class BaseEntity implements Persistable<Long> {

    @Id
    private Long id;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return id == null;
    }

}
