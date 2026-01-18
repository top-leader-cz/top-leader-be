package com.topleader.topleader.user.manager;


import com.topleader.topleader.hr.domain.UserRequest;
import com.topleader.topleader.user.User;
import com.topleader.topleader.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        if (StringUtils.isNotBlank(request.manager())) {
            var manager = userRepository.findByUsername(request.manager())
                    .orElseThrow(() -> new IllegalArgumentException("Manager not found: " + request.manager()));
            user.setManagers(new HashSet<>(Set.of(manager)));
        }
        return userRepository.save(user);
    }


}
