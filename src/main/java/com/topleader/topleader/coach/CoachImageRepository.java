package com.topleader.topleader.coach;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;


/**
 * @author Daniel Slavik
 */
public interface CoachImageRepository extends CrudRepository<CoachImage, Long>, PagingAndSortingRepository<CoachImage, Long> {
    java.util.Optional<CoachImage> findByUsername(String username);
}
