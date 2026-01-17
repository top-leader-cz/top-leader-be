package com.topleader.topleader.coach;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;


/**
 * @author Daniel Slavik
 */
public interface CoachRepository extends CrudRepository<Coach, Long>, PagingAndSortingRepository<Coach, Long> {

    java.util.Optional<Coach> findByUsername(String username);

    @Modifying
    @Query("UPDATE coach SET free_slots = :freeSlots WHERE username = :username")
    void updateCoachSetFreeSlots(String username, boolean freeSlots);

    @Query("SELECT free_slots FROM coach WHERE username = :username")
    boolean hasFeeSlot(String username);
}
