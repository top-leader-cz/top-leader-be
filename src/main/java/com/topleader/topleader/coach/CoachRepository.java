package com.topleader.topleader.coach;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;


/**
 * @author Daniel Slavik
 */
public interface CoachRepository extends JpaRepository<Coach, Long>, JpaSpecificationExecutor<Coach> {

    Optional<Coach> findByUsername(String username);

    @Transactional
    @Modifying
    @Query("update Coach c set c.freeSlots = :freeSlots where c.username = :username")
    void updateCoachSetFreeSlots(String username, boolean freeSlots);

    @Query("select c.freeSlots from Coach c where c.username = :username")
    boolean hasFeeSlot(String username);
}
