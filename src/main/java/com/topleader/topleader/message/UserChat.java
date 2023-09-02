/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.message;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@NoArgsConstructor
@Accessors(chain = true)
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"user1", "user2"})})
public class UserChat {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "chat_id_seq")
    @SequenceGenerator(name = "chat_id_seq", sequenceName = "chat_id_seq", allocationSize = 1)
    private Long chatId;

    private String user1;

    private String user2;
}
