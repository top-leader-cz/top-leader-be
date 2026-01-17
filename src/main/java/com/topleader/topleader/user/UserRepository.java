package com.topleader.topleader.user;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface UserRepository extends CrudRepository<User, String>, PagingAndSortingRepository<User, String> {

     Optional<User> findByEmail(String email);

}
