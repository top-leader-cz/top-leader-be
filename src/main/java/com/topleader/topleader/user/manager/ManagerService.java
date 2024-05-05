package com.topleader.topleader.user.manager;


import com.topleader.topleader.company.Company;
import com.topleader.topleader.hr.domain.ManagerDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ManagerService {

    private final ManagerRepository managerRepository;

     public List<Manager> listManagerByCompany(long companyId) {
        return managerRepository.findByCompanyId(companyId) ;
    }

}
