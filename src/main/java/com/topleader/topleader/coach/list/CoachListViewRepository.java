/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach.list;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Optional;


/**
 * @author Daniel Slavik
 */
public interface CoachListViewRepository extends ListCrudRepository<CoachListView, String>, PagingAndSortingRepository<CoachListView, String> {

    @Query("SELECT * FROM coach_list_view WHERE username = :username")
    Optional<CoachListView> findById(String username);

    @Query("""
        SELECT * FROM coach_list_view
        WHERE public_profile = true
        ORDER BY priority DESC, username ASC
        """)
    List<CoachListView> findAllPublic();

    @Query("""
        SELECT * FROM coach_list_view
        WHERE public_profile = true
        AND (:rates IS NULL OR rate = ANY(string_to_array(:rates, ',')))
        ORDER BY priority DESC, username ASC
        """)
    List<CoachListView> findAllPublicWithRates(String rates);
}
