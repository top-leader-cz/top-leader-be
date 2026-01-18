/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.message;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;


public interface UserChatRepository extends ListCrudRepository<UserChat, Long> {

    @Query("SELECT * FROM user_chat WHERE (user1 = :user1 AND user2 = :user2) OR (user1 = :user2 AND user2 = :user1)")
    Optional<UserChat> findUserChat(String user1, String user2);

    @Query("SELECT * FROM user_chat WHERE user1 = :username OR user2 = :username")
    List<UserChat> findAllForUser(String username);
}
