package com.topleader.topleader.user.manager;


import com.topleader.topleader.company.Company;
import com.topleader.topleader.hr.domain.ManagerDto;
import com.topleader.topleader.hr.domain.UserRequest;
import com.topleader.topleader.user.User;
import com.topleader.topleader.user.UserDetailService;
import com.topleader.topleader.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
            user.setManagers(new HashSet<>(Set.of(new User().setUsername(request.manager()))));
        }
        return userRepository.save(user);
    }


}
