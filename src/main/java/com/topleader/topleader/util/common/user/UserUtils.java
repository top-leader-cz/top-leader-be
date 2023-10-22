package com.topleader.topleader.util.common.user;

import com.topleader.topleader.user.User;
import lombok.experimental.UtilityClass;

@UtilityClass
public class UserUtils {

    public User fromEmail(String email) {
        var split = email.split("@");
        var names = split[0].split("\\.");
        var user = new User().setUsername(email);
        if(names.length == 2) {
            user.setFirstName(names[0]);
            user.setLastName(names[1]);
            return user;
        }
        user.setFirstName(names[0]);
        user.setLastName(names[0]);
        return user;
    }
}
