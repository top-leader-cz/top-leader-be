/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.credit.history;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;


/**
 * @author Daniel Slavik
 */
public interface CreditHistoryRepository extends CrudRepository<CreditHistory, Long>, PagingAndSortingRepository<CreditHistory, Long> {
}
