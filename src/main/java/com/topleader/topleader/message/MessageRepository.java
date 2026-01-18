/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.message;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;


public interface MessageRepository extends ListCrudRepository<Message, Long>, PagingAndSortingRepository<Message, Long> {

    Page<Message> findAllByChatId(Long chatId, Pageable pageable);

    @Modifying
    @Query("UPDATE user_message SET displayed = true WHERE user_to = :username")
    void setAllUserMessagesAsDisplayed(String username);

    @Query("SELECT user_from AS userFrom, COUNT(*) AS unread FROM user_message WHERE user_to = :username AND displayed = false GROUP BY user_from")
    List<UnreadMessagesCount> getUnreadMessagesCount(String username);

    @Query("SELECT DISTINCT user_to FROM user_message WHERE displayed = false AND notified = false")
    List<String> findUndisplayed();

    @Modifying
    @Query("UPDATE user_message SET notified = true WHERE user_to IN (:usernames)")
    void setNotified(List<String> usernames);

}
