package com.topleader.topleader.user.settings;


import com.topleader.topleader.hr.company.Company;
import com.topleader.topleader.hr.company.CompanyRepository;
import com.topleader.topleader.user.UserDetailService;
import com.topleader.topleader.user.manager.UserManagerRepository;
import com.topleader.topleader.user.manager.UsersManagers;
import com.topleader.topleader.user.settings.domain.UserSettings;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserSettingsService {


    private final UserDetailService userDetailService;

    private final CompanyRepository settingsRepository;

    private final UserManagerRepository userManagerRepository;

    public UserSettings getUserSettings(String username) {
        var user = userDetailService.getUser(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        var managerUsername = userManagerRepository.findByUserUsername(username).stream()
                .findFirst()
                .map(UsersManagers::getManagerUsername)
                .orElse(null);

        var userSettings = UserSettings.fromUser(user, managerUsername);
        if (user.getCompanyId() != null) {
            var company = settingsRepository.findById(user.getCompanyId()).orElse(Company.empty());
            userSettings.setCompany(company.getName());
            userSettings.setBusinessStrategy(company.getBusinessStrategy());
        }
        return userSettings;
    }
}
