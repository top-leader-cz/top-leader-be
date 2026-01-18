package com.topleader.topleader.hr;


import com.topleader.topleader.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HrService {

    private final HrViewRepository hrViewRepository;

    public HrView findByUsername(String username) {
        return hrViewRepository.findById(username)
                .orElseThrow(NotFoundException::new);
    }

    public List<HrView> findByCompany(Long companyId) {
        return hrViewRepository.findByCompanyId(companyId);
    }
}
