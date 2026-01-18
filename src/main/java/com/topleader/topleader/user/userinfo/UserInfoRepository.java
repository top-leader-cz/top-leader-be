/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.user.userinfo;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;


/**
 * @author Daniel Slavik
 */
public interface UserInfoRepository extends CrudRepository<UserInfo, Long> {
    Optional<UserInfo> findByUsername(String username);
}
