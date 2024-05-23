package com.topleader.topleader.user.settings.domain;


import com.topleader.topleader.user.User;
import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public class UserSettings {

    private String username;
    private String firstName;
    private String lastName;
    private String company;
    private String manager;
    private String position;
    private String businessStrategy;
    private String aspiredCompetency;
    private String aspiredPosition;

    public static UserSettings fromUser(User user) {
        return new UserSettings()
                .setUsername(user.getUsername())
                .setFirstName(user.getFirstName())
                .setLastName(user.getLastName())
                .setManager(user.getManagers().stream().findFirst().map(User::getUsername).orElse(null))
                .setPosition(user.getPosition())
                .setAspiredCompetency(user.getAspiredCompetency())
                .setAspiredPosition(user.getAspiredPosition());
    }

}
