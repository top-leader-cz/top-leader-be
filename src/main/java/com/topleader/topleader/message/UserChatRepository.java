/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.message;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


/**
 * @author Daniel Slavik
 */
public interface UserChatRepository extends JpaRepository<UserChat, Long> {

    @Query("select ch from UserChat ch where (ch.user1 = :user1 and ch.user2 = :user2) or (ch.user1 = :user2 and ch.user2 = :user1)")
    Optional<UserChat> findUserChat(String user1, String user2);

    @Query("select ch from UserChat ch where ch.user1 = :username or ch.user2 = :username")
    List<UserChat> findAllForUser(String username);
}
