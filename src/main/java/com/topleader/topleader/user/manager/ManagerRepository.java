package com.topleader.topleader.user.manager;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface ManagerRepository extends CrudRepository<Manager, String>, PagingAndSortingRepository<Manager, String> {

    List<Manager> findByCompanyId(long companyId);

}
