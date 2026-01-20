package com.topleader.topleader.user.manager;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface UserManagerRepository extends ListCrudRepository<UsersManagers, Long> {

    @Modifying
    @Query("DELETE FROM users_managers WHERE manager_username = :username")
    void cleanUpManagers(String username);

    @Query("SELECT * FROM users_managers WHERE user_username = :userUsername")
    List<UsersManagers> findByUserUsername(String userUsername);
}
