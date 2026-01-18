/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.message;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;


public interface LastMessageRepository extends ListCrudRepository<LastMessage, Long> {

    @Query("SELECT * FROM last_message WHERE chat_id IN (:ids)")
    List<LastMessage> findAllByChatIdIn(Collection<Long> ids);

    @Modifying
    @Query("INSERT INTO last_message (chat_id, message_id) VALUES (:chatId, :messageId) " +
           "ON CONFLICT (chat_id) DO UPDATE SET message_id = :messageId")
    void upsert(Long chatId, Long messageId);
}
