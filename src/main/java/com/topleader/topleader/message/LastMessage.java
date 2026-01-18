/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.message;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;


@Getter
@Setter
@ToString
@NoArgsConstructor
@Accessors(chain = true)
@Table(name = "last_message")
public class LastMessage implements Persistable<Long> {

    @Id
    private Long chatId;

    private Long messageId;

    @Transient
    private boolean isNew = true;

    @Override
    public Long getId() {
        return chatId;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    public LastMessage markAsExisting() {
        this.isNew = false;
        return this;
    }

}
