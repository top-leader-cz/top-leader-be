package com.topleader.topleader.user.userinsight;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface UserInsightRepository extends CrudRepository<UserInsight, Long>, PagingAndSortingRepository<UserInsight, Long> {

    Optional<UserInsight> findByUsername(String username);
}
