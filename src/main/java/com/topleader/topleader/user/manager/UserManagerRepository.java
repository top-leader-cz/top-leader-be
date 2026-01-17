package com.topleader.topleader.user.manager;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface UserManagerRepository extends CrudRepository<UsersManagers, Long>, PagingAndSortingRepository<UsersManagers, Long> {

    @Modifying
    @Query("DELETE FROM users_managers WHERE manager_username = :username")
    void cleanUpManagers(String username);

    List<UsersManagers> findByUserUsername(String userUsername);
}
