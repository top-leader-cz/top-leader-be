package com.topleader.topleader.user;

import java.util.List;
import java.util.Optional;

import com.google.api.services.sqladmin.SQLAdmin;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;

public interface UserRepository extends JpaRepository<User, String> {

     Optional<User> findByEmail(String email);

}
