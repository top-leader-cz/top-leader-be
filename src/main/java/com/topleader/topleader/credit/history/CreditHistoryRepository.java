package com.topleader.topleader.credit.history;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;


public interface CreditHistoryRepository extends ListCrudRepository<CreditHistory, Long> {
}
