package com.topleader.topleader.user.util;

import com.topleader.topleader.user.User;
import java.util.Locale;

import lombok.experimental.UtilityClass;

@UtilityClass
public class UserUtils {

    public User fromEmail(String email) {
        var split = email.split("@");
        var names = split[0].split("\\.");
        var user = new User()
                .setUsername(email.toLowerCase(Locale.ROOT))
                .setEmail(email.toLowerCase(Locale.ROOT));
        if(names.length == 2) {
            user.setFirstName(names[0]);
            user.setLastName(names[1]);
            return user;
        }
        user.setFirstName(names[0]);
        user.setLastName(names[0]);
        return user;
    }

    public static boolean shouldSendInvite(User.Status oldStatus, User.Status newStatus) {
        return User.Status.PENDING == oldStatus && (User.Status.AUTHORIZED == newStatus || User.Status.PAID == newStatus);
    }
}
