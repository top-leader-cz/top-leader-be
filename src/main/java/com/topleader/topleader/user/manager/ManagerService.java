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
        var authorities = new java.util.HashSet<>(user.getAuthorities());
        if (request.isManager()) {
            authorities.add(User.Authority.MANAGER);
        } else {
            authorities.remove(User.Authority.MANAGER);
            userManagerRepository.cleanUpManagers(user.getUsername());
        }
        user.setAuthorities(authorities);

        if (StringUtils.isNotBlank(request.manager())) {
            var manager = userRepository.findByUsername(request.manager())
                    .orElseThrow(() -> new IllegalArgumentException("Manager not found: " + request.manager()));

            // Delete existing manager relationships for this user
            userManagerRepository.findByUserUsername(user.getUsername())
                    .forEach(um -> userManagerRepository.delete(um));

            // Create new manager relationship
            userManagerRepository.save(new UsersManagers()
                    .setUserUsername(user.getUsername())
                    .setManagerUsername(manager.getUsername()));
        }
        return userRepository.save(user);
    }


}
