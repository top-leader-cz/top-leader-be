package com.topleader.topleader.hr;


import com.topleader.topleader.user.User;
import com.topleader.topleader.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HrService {

    private final HrViewRepository hrViewRepository;

    public HrView findByUsername(String username) {
        return hrViewRepository.findById(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));
    }

    public List<HrView> findByCompany(Long companyId) {
        return hrViewRepository.findAllByCompanyId(companyId);
    }
}
