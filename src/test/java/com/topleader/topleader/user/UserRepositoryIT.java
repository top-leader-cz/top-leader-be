/*
 * Copyright (c) 2024 Price f(x), s.r.o.
 */
package com.topleader.topleader.user;

import com.topleader.topleader.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Sql(scripts = {"/sql/user/user-repository-test.sql"})
class UserRepositoryIT extends IntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindAllUsersByUsernameIn() {
        // Given a collection of usernames
        var usernames = List.of("test.user1@gmail.com", "test.user2@gmail.com", "test.user3@gmail.com");

        // When finding users by usernames
        var users = userRepository.findAllByUsernameIn(usernames);

        // Then should return all matching users
        assertThat("Should find 3 users", users.size(), is(3));
        assertThat("Should contain all requested users",
                users.stream().map(User::getUsername).toList(),
                containsInAnyOrder("test.user1@gmail.com", "test.user2@gmail.com", "test.user3@gmail.com"));
    }

    @Test
    void shouldReturnEmptyListWhenNoUsersMatch() {
        // Given usernames that don't exist
        var usernames = List.of("nonexistent1@gmail.com", "nonexistent2@gmail.com");

        // When finding users by usernames
        var users = userRepository.findAllByUsernameIn(usernames);

        // Then should return empty list
        assertThat("Should return empty list", users, is(empty()));
    }

    @Test
    void shouldFindPartialMatches() {
        // Given a mix of existing and non-existing usernames
        var usernames = List.of("test.user1@gmail.com", "nonexistent@gmail.com", "test.user2@gmail.com");

        // When finding users by usernames
        var users = userRepository.findAllByUsernameIn(usernames);

        // Then should return only matching users
        assertThat("Should find only 2 users", users.size(), is(2));
        assertThat("Should contain only existing users",
                users.stream().map(User::getUsername).toList(),
                containsInAnyOrder("test.user1@gmail.com", "test.user2@gmail.com"));
    }

    @Test
    void shouldFindUserByUsername() {
        // When finding user by username
        var user = userRepository.findByUsername("test.user1@gmail.com");

        // Then should return the user
        assertThat("User should be present", user.isPresent(), is(true));
        assertThat("Username should match", user.get().getUsername(), is("test.user1@gmail.com"));
        assertThat("First name should match", user.get().getFirstName(), is("Test"));
        assertThat("Last name should match", user.get().getLastName(), is("User1"));
    }

    @Test
    void shouldNotFindNonExistentUser() {
        // When finding user that doesn't exist
        var user = userRepository.findByUsername("nonexistent@gmail.com");

        // Then should return empty
        assertThat("User should not be present", user.isEmpty(), is(true));
    }

    @Test
    void shouldFindByCompanyIdExcludingOnlyCanceledWithoutSessions() {
        // When finding users by company id
        var users = userRepository.findActiveByCompanyId(100L);

        var usernames = users.stream().map(User::getUsername).toList();

        // Then should include all non-canceled users
        assertThat("Should contain non-canceled users", usernames,
                hasItems("test.user1@gmail.com", "test.user2@gmail.com", "test.user3@gmail.com"));

        // And should include CANCELED users that have UPCOMING/COMPLETED sessions
        assertThat("Should contain canceled user with UPCOMING session", usernames,
                hasItem("canceled.with.upcoming@gmail.com"));
        assertThat("Should contain canceled user with COMPLETED session", usernames,
                hasItem("canceled.with.completed@gmail.com"));

        // But should exclude CANCELED users without any UPCOMING/COMPLETED sessions
        assertThat("Should not contain canceled user without sessions", usernames,
                not(hasItem("canceled.no.session@gmail.com")));

        assertThat("Should find 5 users total", users.size(), is(5));
    }
}
