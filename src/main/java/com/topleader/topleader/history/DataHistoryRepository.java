/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.history;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;


/**
 * @author Daniel Slavik
 */
public interface DataHistoryRepository extends CrudRepository<DataHistory, Long>, PagingAndSortingRepository<DataHistory, Long> {

    @Query("SELECT * FROM data_history WHERE username = :username AND type = :type")
    List<DataHistory> findAllByUsernameAndType(String username, String type);

    default List<DataHistory> findAllByUsernameAndType(String username, DataHistory.Type type) {
        return findAllByUsernameAndType(username, type.name());
    }

    @Query("SELECT * FROM data_history WHERE username = :username AND type = :type ORDER BY id DESC LIMIT 1")
    Optional<DataHistory> findTopByUsernameAndTypeOrderByIdDesc(String username, String type);

    default Optional<DataHistory> findTopByUsernameAndTypeOrderByIdDesc(String username, DataHistory.Type type) {
        return findTopByUsernameAndTypeOrderByIdDesc(username, type.name());
    }
}
