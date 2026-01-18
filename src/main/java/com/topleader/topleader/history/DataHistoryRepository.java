package com.topleader.topleader.history;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.Optional;

public interface DataHistoryRepository extends ListCrudRepository<DataHistory, Long> {

    @Query("SELECT * FROM data_history WHERE username = :username AND type = :type")
    List<DataHistory> findByUsernameAndType(String username, String type);

    @Query("SELECT * FROM data_history WHERE username = :username AND type = :type ORDER BY id DESC LIMIT 1")
    Optional<DataHistory> findTopByUsernameAndType(String username, String type);
}
