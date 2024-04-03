package com.topleader.topleader.user.manager;


import com.topleader.topleader.company.Company;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ManagerService {

    private final ManagerRepository managerRepository;

    public void addManager(String username) {
        managerRepository.save(new Manager().setUsername(username));
    }

    public List<Manager> listManagerByCompany(long companyId) {
        return managerRepository.findByCompanyId(companyId) ;
    }
}
