package com.topleader.topleader.user.badge;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface BadgeRepository extends CrudRepository<Badge, Long>, PagingAndSortingRepository<Badge, Long> {

    @Query("SELECT * FROM badge WHERE username = :username AND year = :year")
    List<Badge> getUserBadges(String username, int year);
}
