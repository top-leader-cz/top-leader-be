package com.topleader.topleader.user.userinsight;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface UserInsightRepository extends CrudRepository<UserInsight, String>, PagingAndSortingRepository<UserInsight, String> {

}
