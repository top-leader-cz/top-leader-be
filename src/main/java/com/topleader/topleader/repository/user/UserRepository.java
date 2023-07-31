package com.topleader.topleader.repository.user;

import com.topleader.topleader.entity.user.User;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
}
