package com.topleader.topleader.coach;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;


/**
 * @author Daniel Slavik
 */
public interface CoachRepository extends CrudRepository<Coach, Long>, PagingAndSortingRepository<Coach, Long> {

    Optional<Coach> findByUsername(String username);
}


