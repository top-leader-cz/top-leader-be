package com.topleader.topleader.user.settings.domain;


import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UserSettingRequest {

    @NotEmpty
    private String firstName;
    @NotEmpty
    private String lastName;
    private String manager;
    private String position;
    private String aspiredCompetency;
    private String aspiredPosition;
    private String businessStrategy;
}
