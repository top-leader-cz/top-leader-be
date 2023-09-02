/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.message;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;


/**
 * @author Daniel Slavik
 */
public interface LastMessageRepository extends JpaRepository<LastMessage, Long> {
    List<LastMessage> findAllByChatIdIn(Collection<Long> ids);
}
