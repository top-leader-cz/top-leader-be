/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.repository.history;

import com.topleader.topleader.entity.history.DataHistory;
import java.util.List;
import org.springframework.data.repository.CrudRepository;


/**
 * @author Daniel Slavik
 */
public interface DataHistoryRepository extends CrudRepository<DataHistory, String> {

    List<DataHistory> findAllByUsernameAndType(String username, DataHistory.Type type);
}
