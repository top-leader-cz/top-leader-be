/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.message;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.transaction.Transactional;
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
    @Query("update Message set displayed = true where userTo = :username")
    void setAllUserMessagesAsDisplayed(String username);

    @Query("select m.userFrom as userFrom, count(*) as unread from Message m where m.userTo = :username and m.displayed = false group by m.userFrom")
    List<UnreadMessagesCount> getUnreadMessagesCount(String username);


    @Query("select m.userTo from Message m  where m.displayed = false  and m.notified = false and m.createdAt < :interval group by m.userTo")
    List<String> findUnDisplayedMoreThenFourHours(LocalDateTime interval);

    @Modifying
    @Transactional
    @Query("update Message m set m.notified = true where m.userTo in(:username)")
    void setNotified(List<String> username);


}
