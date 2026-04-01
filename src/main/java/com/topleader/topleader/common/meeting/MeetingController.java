package com.topleader.topleader.common.meeting;

import com.topleader.topleader.common.meeting.domain.MeetingInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/latest/meeting")
@Secured({"COACH"})
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;

    @GetMapping
    public MeetingSettingsResponse getSettings(@AuthenticationPrincipal UserDetails user) {
        return meetingService.getInfo(user.getUsername())
                .map(info -> new MeetingSettingsResponse(
                        info.getProvider(),
                        info.getEmail(),
                        info.isAutoGenerate(),
                        info.getStatus()))
                .orElse(MeetingSettingsResponse.NOT_CONNECTED);
    }

    @PatchMapping
    public void updateSettings(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody UpdateAutoGenerateRequest request
    ) {
        meetingService.updateAutoGenerate(user.getUsername(), request.autoGenerate());
    }

    @DeleteMapping
    public void disconnect(@AuthenticationPrincipal UserDetails user) {
        meetingService.disconnect(user.getUsername());
    }

    public record MeetingSettingsResponse(MeetingInfo.Provider provider, String email, boolean autoGenerate, MeetingInfo.Status status) {
        static final MeetingSettingsResponse NOT_CONNECTED = new MeetingSettingsResponse(null, null, false, null);
    }

    public record UpdateAutoGenerateRequest(boolean autoGenerate) {
    }
}
