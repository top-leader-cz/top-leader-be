package com.topleader.topleader.coach.client;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;


/**
 * @author Daniel Slavik
 */
public interface CoachClientViewRepository extends CrudRepository<CoachClientView, String>, PagingAndSortingRepository<CoachClientView, String> {

    List<CoachClientView> findAllByCoach(String coach);
}
