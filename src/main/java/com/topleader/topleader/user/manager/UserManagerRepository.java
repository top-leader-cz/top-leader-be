package com.topleader.topleader.user.manager;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface UserManagerRepository extends JpaRepository<UsersManagers, Long> {

    @Modifying
    @Query("delete from UsersManagers u where u.manager.username = :username")
    void cleanUpManagers(String username);
}
