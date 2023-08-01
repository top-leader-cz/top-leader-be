/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.user;

import org.springframework.data.repository.CrudRepository;


/**
 * @author Daniel Slavik
 */
public interface UserInfoRepository extends CrudRepository<UserInfo, String> {
}
