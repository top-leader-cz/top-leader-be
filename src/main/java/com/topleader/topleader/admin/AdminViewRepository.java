package com.topleader.topleader.admin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


/**
 * @author Daniel Slavik
 */
public interface AdminViewRepository extends JpaRepository<AdminView, Long>, JpaSpecificationExecutor<AdminView> {

}
