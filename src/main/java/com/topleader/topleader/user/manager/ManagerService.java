package com.topleader.topleader.user.manager;


import com.topleader.topleader.hr.domain.UserRequest;
import com.topleader.topleader.user.User;
import com.topleader.topleader.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ManagerService {

    private final ManagerRepository managerRepository;

    private final UserRepository userRepository;

    private final UserManagerRepository userManagerRepository;

    public List<Manager> listManagerByCompany(long companyId) {
        return managerRepository.findByCompanyId(companyId);
    }

    public User processUser(User user, UserRequest request) {
        if (request.isManager()) {
            user.getAuthorities().add(User.Authority.MANAGER);
        } else {
            user.getAuthorities().remove(User.Authority.MANAGER);
            userManagerRepository.cleanUpManagers(user.getUsername());
        }

        // Manager relationship handled via UsersManagers table
        return userRepository.save(user);
    }


}
