package com.topleader.topleader.hr.company;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;

    public void setCompanyBusinessStrategy(long companyId, String strategy) {
        companyRepository.updateStrategy(companyId, strategy);
    }
}
