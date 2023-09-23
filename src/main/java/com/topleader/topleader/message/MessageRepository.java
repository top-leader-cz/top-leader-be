/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.message;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;


/**
 * @author Daniel Slavik
 */
public interface MessageRepository extends JpaRepository<Message, Long> {

    Page<Message> findAllByChatId(Long chatId, Pageable pageable);
    @Modifying
    @Query("update Message set displayed = true where userFrom = :username and userTo = :addressee")
    void setAllUserMessagesAsDisplayed(String username, String addressee);

    @Query("select m.userFrom as userFrom, count(*) as unread from Message m where m.userTo = :username and m.displayed = false group by m.userFrom")
    List<UnreadMessagesCount> getUnreadMessagesCount(String username);

}
