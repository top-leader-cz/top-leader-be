package com.topleader.topleader.user.settings;



import com.topleader.topleader.hr.company.CompanyService;
import com.topleader.topleader.hr.domain.ManagerDto;
import com.topleader.topleader.user.UserDetailService;
import com.topleader.topleader.user.UserRepository;
import com.topleader.topleader.user.manager.ManagerService;
import com.topleader.topleader.user.manager.UserManagerRepository;
import com.topleader.topleader.user.manager.UsersManagers;
import com.topleader.topleader.user.settings.domain.UserSettingRequest;
import com.topleader.topleader.user.settings.domain.UserSettings;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import static com.topleader.topleader.common.util.user.UserDetailUtils.isHr;


@Slf4j
@RestController
@RequestMapping("/api/latest/user-settings")
@AllArgsConstructor
public class UserSettingsController {

    private final UserDetailService userDetailService;

    private final UserSettingsService settingsService;

    private final ManagerService managerService;

    private final CompanyService companyService;

    private final UserRepository userRepository;

    private final UserManagerRepository userManagerRepository;

    @Secured({"USER"})
    @GetMapping("/managers")
    public List<ManagerDto> listManagers(@AuthenticationPrincipal UserDetails user) {
        var foundUser = userDetailService.find(user.getUsername());
        if (foundUser.getCompanyId() == null) {
            log.warn("User {} has no companyId, returning empty list", user.getUsername());
            return List.of();
        }
        return managerService.listManagerByCompany(foundUser.getCompanyId()).stream()
                .map(m -> {
                    var found = userDetailService.find(m.getUsername());
                    return new ManagerDto(found.getUsername(), found.getEmail(), found.getFirstName(), found.getLastName());
                })
                .collect(Collectors.toList());
    }

    @Secured({"USER"})
    @GetMapping
    public UserSettings fetchUserSetting(@AuthenticationPrincipal UserDetails loggedUser) {
        return settingsService.getUserSettings(loggedUser.getUsername());
    }

    @Secured({"USER"})
    @PutMapping
    public UserSettings updateUserSetting(
        @AuthenticationPrincipal UserDetails loggedUser,
        @RequestBody @Valid UserSettingRequest request
    ) {
         var user = userDetailService.getUser(loggedUser.getUsername())
            .orElseThrow(() -> new UsernameNotFoundException(loggedUser.getUsername()));


        if (isHr(loggedUser)) {
           Optional.ofNullable(user.getCompanyId()).ifPresentOrElse(
                companyId -> companyService.setCompanyBusinessStrategy(companyId, request.getBusinessStrategy()),
                () -> log.info("User {} is not part of any company. Cannot update business strategy.", loggedUser.getUsername()
            ));
        }

        user.setFirstName(request.getFirstName())
                .setLastName(request.getLastName())
                .setEmail(request.getEmail())
                .setPosition(request.getPosition())
                .setAspiredPosition(request.getAspiredPosition())
                .setAspiredCompetency(request.getAspiredCompetency());

        var savedUser = userDetailService.save(user);

        String managerUsername = null;
        if (StringUtils.isNotBlank(request.getManager())) {
            var manager = userRepository.findByUsername(request.getManager())
                    .orElseThrow(() -> new IllegalArgumentException("Manager not found: " + request.getManager()));

            // Delete existing manager relationships for this user
            userManagerRepository.findByUserUsername(loggedUser.getUsername())
                    .forEach(um -> userManagerRepository.delete(um));

            // Create new manager relationship
            userManagerRepository.save(new UsersManagers()
                    .setUserUsername(loggedUser.getUsername())
                    .setManagerUsername(manager.getUsername()));
            managerUsername = manager.getUsername();
        }

        return UserSettings.fromUser(savedUser, managerUsername);
    }


}
