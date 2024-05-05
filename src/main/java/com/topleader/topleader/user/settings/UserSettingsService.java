package com.topleader.topleader.user.settings;


import com.topleader.topleader.company.Company;
import com.topleader.topleader.company.CompanyRepository;
import com.topleader.topleader.user.UserDetailService;
import com.topleader.topleader.user.settings.domain.UserSettings;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserSettingsService {


    private final UserDetailService userDetailService;

    private final CompanyRepository settingsRepository;

    public UserSettings getUserSettings(String username) {
        var user = userDetailService.getUser(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        var userSettings =  UserSettings.fromUser(user);
        if(userSettings.getCompanyId() != null) {
            var company = settingsRepository.findById(userSettings.getCompanyId()).orElse(Company.empty());
            userSettings.setBusinessStrategy(company.getBusinessStrategy());
        }
        return userSettings;
    }
}
