package com.topleader.topleader.coach.rate;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;


/**
 * @author Daniel Slavik
 */
public interface CoachRateRepository extends CrudRepository<CoachRate, String>, PagingAndSortingRepository<CoachRate, String> {
}
