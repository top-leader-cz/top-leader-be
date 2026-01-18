package com.topleader.topleader.user.settings.domain;


import com.topleader.topleader.user.User;
import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public class UserSettings {

    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String company;
    private String manager;
    private String position;
    private String businessStrategy;
    private String aspiredCompetency;
    private String aspiredPosition;

    public static UserSettings fromUser(User user, String managerUsername) {
        return new UserSettings()
                .setUsername(user.getUsername())
                .setEmail(user.getEmail())
                .setFirstName(user.getFirstName())
                .setLastName(user.getLastName())
                .setManager(managerUsername)
                .setPosition(user.getPosition())
                .setAspiredCompetency(user.getAspiredCompetency())
                .setAspiredPosition(user.getAspiredPosition());
    }

}
