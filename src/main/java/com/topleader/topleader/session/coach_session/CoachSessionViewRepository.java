package com.topleader.topleader.session.coach_session;


import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CoachSessionViewRepository extends ListCrudRepository<CoachSessionView, Long>,
                                                      PagingAndSortingRepository<CoachSessionView, Long> {

    List<CoachSessionView> findByCoachUsername(String coachUsername);

    @Query("""
        SELECT * FROM coach_session_view
        WHERE coach_username = :coachUsername
        AND (COALESCE(:client, '') = '' OR client = :client)
        AND (COALESCE(:status, '') = '' OR status = :status)
        AND (CAST(:fromDate AS timestamp) IS NULL OR date >= :fromDate)
        AND (CAST(:toDate AS timestamp) IS NULL OR date <= :toDate)
        """)
    List<CoachSessionView> findFiltered(String coachUsername, String client, String status, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);

    @Query("""
        SELECT COUNT(*) FROM coach_session_view
        WHERE coach_username = :coachUsername
        AND (COALESCE(:client, '') = '' OR client = :client)
        AND (COALESCE(:status, '') = '' OR status = :status)
        AND (CAST(:fromDate AS timestamp) IS NULL OR date >= :fromDate)
        AND (CAST(:toDate AS timestamp) IS NULL OR date <= :toDate)
        """)
    long countFiltered(String coachUsername, String client, String status, LocalDateTime fromDate, LocalDateTime toDate);
}

