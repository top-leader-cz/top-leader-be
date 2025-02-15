package com.topleader.topleader.coach.availability.settings;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Entity
@Accessors(chain = true)
public class CoachAvailabilitySettings {

    @Id
    private String username;

    private AvailabilitySettingsType type;

    boolean active;


    public enum AvailabilitySettingsType {
        LOCAL,
        GOOGLE,
        CALENDLY,
    }
}
