package com.topleader.topleader.user;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface UserRepository extends CrudRepository<User, Long>, PagingAndSortingRepository<User, Long> {

     Optional<User> findByEmail(String email);

     Optional<User> findByUsername(String username);

     List<User> findAllByUsernameIn(Collection<String> usernames);

}
