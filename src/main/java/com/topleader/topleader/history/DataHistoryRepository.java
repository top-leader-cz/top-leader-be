/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.history;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;


/**
 * @author Daniel Slavik
 */
public interface DataHistoryRepository extends CrudRepository<DataHistory, String> {

    List<DataHistory> findAllByUsernameAndType(String username, DataHistory.Type type);

    Optional<DataHistory> findTopByUsernameAndTypeOrderByIdDesc(String username, DataHistory.Type type);
}
