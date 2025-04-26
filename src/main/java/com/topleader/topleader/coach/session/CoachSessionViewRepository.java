package com.topleader.topleader.coach.session;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoachSessionViewRepository extends JpaRepository<CoachSessionView, Long>, JpaSpecificationExecutor<CoachSessionView> {

    @Query("select distinct new com.topleader.topleader.coach.session.Client(c.client, c.firstName, c.lastName) from CoachSessionView c " +
            "where c.coachUsername = :coach")
    List<Client> fetchClients(String coach);
}
