package com.topleader.topleader.coach;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Optional;


/**
 * @author Daniel Slavik
 */
public interface CoachRepository extends CrudRepository<Coach, Long>, PagingAndSortingRepository<Coach, Long> {

    Optional<Coach> findByUsername(String username);

    @Query("""
            SELECT DISTINCT jsonb_array_elements_text(languages) AS language
            FROM coach
            WHERE languages IS NOT NULL
            ORDER BY language
            """)
    List<String> findDistinctLanguages();
}


