package com.topleader.topleader.user.settings;


import com.topleader.topleader.hr.domain.ManagerDto;
import com.topleader.topleader.user.User;
import com.topleader.topleader.user.UserDetailService;
import com.topleader.topleader.user.manager.Manager;
import com.topleader.topleader.user.manager.ManagerService;
import com.topleader.topleader.user.settings.domain.UserSettingRequest;
import com.topleader.topleader.user.settings.domain.UserSettings;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/api/latest/user-settings")
@AllArgsConstructor
public class UserSettingsController {

    private final UserDetailService userDetailService;

    private final UserSettingsService settingsService;

    private final ManagerService managerService;

    @Secured({"USER"})
    @GetMapping("/managers")
    @Transactional
    public List<ManagerDto> listManagers(@AuthenticationPrincipal UserDetails user) {
        var foundUser = userDetailService.find(user.getUsername());
        return managerService.listManagerByCompany(foundUser.getCompanyId()).stream()
                .map(m -> {
                    var found = userDetailService.find(m.getUsername());
                    return new ManagerDto(found.getUsername(), found.getFirstName(), found.getLastName());
                })
                .collect(Collectors.toList());
    }

    @Secured({"USER"})
    @GetMapping
    @Transactional
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

        user.setFirstName(request.getFirstName())
                .setLastName(request.getLastName())
                .setManagers(Set.of(new User().setUsername(request.getManager())))
                .setPosition(request.getPosition())
                .setAspiredPosition(request.getAspiredPosition())
                .setAspiredCompetency(request.getAspiredCompetency());

        return UserSettings.fromUser(userDetailService.save(user));
    }
}
