package com.topleader.topleader.session.coach_session;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CoachSessionViewRepository extends CrudRepository<CoachSessionView, Long>, PagingAndSortingRepository<CoachSessionView, Long> {

    @Query("SELECT DISTINCT client, first_name, last_name FROM coach_session_view WHERE coach_username = :coach")
    List<Client> fetchClients(String coach);

    List<CoachSessionView> findAllByCoachUsername(String coachUsername);

    List<CoachSessionView> findAllByCoachUsernameAndStatusIn(String coachUsername, List<String> statuses);

    List<CoachSessionView> findAllByCoachUsernameAndClientIn(String coachUsername, List<String> clients);

    List<CoachSessionView> findAllByCoachUsernameAndDateBetween(String coachUsername, LocalDateTime from, LocalDateTime to);

    @Query("""
        SELECT * FROM coach_session_view
        WHERE coach_username = :coachUsername
        AND (:client IS NULL OR client = :client)
        AND (:status IS NULL OR status = :status)
        AND (:from IS NULL OR date >= :from)
        AND (:to IS NULL OR date <= :to)
        ORDER BY date DESC
        """)
    List<CoachSessionView> findFiltered(String coachUsername, String client, String status, LocalDateTime from, LocalDateTime to);
}
