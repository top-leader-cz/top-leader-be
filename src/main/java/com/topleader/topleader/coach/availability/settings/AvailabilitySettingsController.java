package com.topleader.topleader.coach.availability.settings;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@RequestMapping("/api/latest/coach-availability-settings")
public class AvailabilitySettingsController {

    public AvailabilitySettingRepository repository;


    @PostMapping
    public AvailabilitySettingsDto set(@RequestMapping AvailabilitySettingsDto settings) {
        return repository.save(new CoachAvailabilitySettings().setUsername().se);
    }


    public record AvailabilitySettingsDto(
        CoachAvailabilitySettings.AvailabilitySettingsType type,
       ) {
    }

}
