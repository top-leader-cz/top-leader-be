package com.topleader.topleader.myteam;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;


/**
 * @author Daniel Slavik
 */
public interface MyTeamViewRepository extends ListCrudRepository<MyTeamView, String>, PagingAndSortingRepository<MyTeamView, String> {

    Page<MyTeamView> findAllByManager(String manager, Pageable pageable);
}
