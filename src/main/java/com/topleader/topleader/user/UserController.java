package com.topleader.topleader.user;

import com.topleader.topleader.email.EmailService;
import com.topleader.topleader.email.VelocityService;
import com.topleader.topleader.user.userinfo.UserInfo;
import com.topleader.topleader.user.userinfo.UserInfoService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/latest/user")
@AllArgsConstructor
public class UserController {

    private final UserDetailService userDetailService;

    private final InvitationService invitationService;


    @PostMapping
    public void addMember(@RequestBody AddUserRequest request) {
        var user = new User().setUsername(request.getUsername())
                .setUserInfo(new UserInfo().setStatus(request.getStatus()));

        userDetailService.addUser(user);
        if(sendInvite(request.getStatus())) {
            invitationService.sendInvite(request);
        }
    }

    private boolean sendInvite(UserInfo.Status status) {
        return UserInfo.Status.AUTHORIZED == status|| UserInfo.Status.PAID == status;
    }

    @Data
    public static class AddUserRequest {
        private String firstName;

        private String lastName;

        private String username;

        private UserInfo.Status status;

        private String locale;

        public String getLocale() {
            if("cs".equals(locale) || "fr".equals(locale)) {
                return locale;
            }
            return "en";
        }
    }
}
