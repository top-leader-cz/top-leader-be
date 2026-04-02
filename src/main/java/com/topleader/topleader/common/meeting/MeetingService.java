package com.topleader.topleader.common.meeting;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.topleader.topleader.common.calendar.google.GoogleCalendarApiClientFactory;
import com.topleader.topleader.common.exception.NotFoundException;
import com.topleader.topleader.common.meeting.domain.MeetingInfo;
import com.topleader.topleader.common.meeting.google.GoogleMeetApiClient;
import com.topleader.topleader.common.meeting.zoom.ZoomApiClient;
import com.topleader.topleader.common.notification.Notification;
import com.topleader.topleader.common.notification.NotificationService;
import com.topleader.topleader.common.notification.context.MeetLinkFailedNotificationContext;
import com.topleader.topleader.common.util.crypto.TokenEncryptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MeetingService {

    private final MeetingInfoRepository repository;

    private final NotificationService notificationService;

    private final TokenEncryptor tokenEncryptor;

    private final Map<MeetingInfo.Provider, MeetLinkGenerator> generators;

    public MeetingService(
            MeetingInfoRepository repository,
            GoogleCalendarApiClientFactory tokenFactory,
            GoogleMeetApiClient meetApiClient,
            ZoomApiClient zoomApiClient,
            NotificationService notificationService,
            TokenEncryptor tokenEncryptor
    ) {
        this.repository = repository;
        this.notificationService = notificationService;
        this.tokenEncryptor = tokenEncryptor;
        this.generators = Map.of(
                MeetingInfo.Provider.GOOGLE, new GoogleMeetLinkGenerator(tokenFactory, meetApiClient, repository, tokenEncryptor),
                MeetingInfo.Provider.ZOOM, new ZoomMeetLinkGenerator(zoomApiClient, repository, tokenEncryptor)
        );
    }

    public void storeConnection(String username, MeetingInfo.Provider provider, String refreshToken, String accessToken, String email) {
        log.info("Storing {} connection for user {}", provider, username);
        repository.upsertConnection(username, provider.name(), tokenEncryptor.encrypt(refreshToken), tokenEncryptor.encrypt(accessToken), email);
        log.info("{} connection stored for user {}", provider, username);
    }

    public Optional<MeetingInfo> getInfo(String username) {
        return repository.findByUsername(username);
    }

    public void disconnect(String username) {
        repository.deleteByUsername(username);
        log.info("Meeting provider disconnected for user {}", username);
    }

    public void updateAutoGenerate(String username, boolean autoGenerate) {
        repository.findByUsername(username).orElseThrow(NotFoundException::new);
        repository.updateAutoGenerate(username, autoGenerate);
    }

    public Optional<MeetLinkResult> generateMeetLinkIfEnabled(Long sessionId, String coachUsername, LocalDateTime sessionTime, String clientName) {
        return repository.findByUsername(coachUsername)
                .filter(MeetingInfo::isAutoGenerate)
                .filter(info -> info.getStatus() != MeetingInfo.Status.ERROR)
                .flatMap(info -> Optional.ofNullable(generators.get(info.getProvider()))
                        .flatMap(generator -> tryGenerate(generator, info, sessionId, coachUsername, sessionTime, clientName))
                        .map(link -> new MeetLinkResult(link, info.getProvider())));
    }

    private Optional<String> tryGenerate(MeetLinkGenerator generator, MeetingInfo info, Long sessionId, String coachUsername, LocalDateTime sessionTime, String clientName) {
        try {
            return generator.generate(info, sessionId, sessionTime, clientName);
        } catch (Exception e) {
            log.error("Failed to generate meeting link via {} for coach {}", info.getProvider(), coachUsername, e);
            handleMeetLinkFailure(info, coachUsername, clientName, sessionTime);
            return Optional.empty();
        }
    }

    private void handleMeetLinkFailure(MeetingInfo info, String coachUsername, String clientName, LocalDateTime sessionTime) {
        var newStatus = info.getStatus() == MeetingInfo.Status.OK
                ? MeetingInfo.Status.WARN
                : MeetingInfo.Status.ERROR;
        repository.updateStatus(info.getUsername(), newStatus.name());

        notificationService.addNotification(new NotificationService.CreateNotificationRequest(
                coachUsername,
                Notification.Type.MEET_LINK_FAILED,
                new MeetLinkFailedNotificationContext()
                        .setClientName(clientName)
                        .setSessionTime(sessionTime.toString())
        ));
    }

    public record MeetLinkResult(String link, MeetingInfo.Provider provider) {

        public String providerLabel() {
            return switch (provider) {
                case GOOGLE -> "Google Meet";
                case ZOOM -> "Zoom";
                case TEAMS -> "Microsoft Teams";
            };
        }
    }

    @FunctionalInterface
    interface MeetLinkGenerator {
        Optional<String> generate(MeetingInfo info, Long sessionId, LocalDateTime sessionTime, String clientName);
    }

    private record GoogleMeetLinkGenerator(
            GoogleCalendarApiClientFactory tokenFactory,
            GoogleMeetApiClient meetApiClient,
            MeetingInfoRepository repository,
            TokenEncryptor encryptor
    ) implements MeetLinkGenerator {

        @Override
        public Optional<String> generate(MeetingInfo info, Long sessionId, LocalDateTime sessionTime, String clientName) {
            return Optional.ofNullable(tokenFactory.refreshToken(encryptor.decrypt(info.getRefreshToken())))
                    .map(tokenResponse -> {
                        persistTokens(info.getUsername(), tokenResponse.refreshToken(), tokenResponse.accessToken());
                        return meetApiClient.createEventWithMeet(
                                tokenResponse.accessToken(),
                                "TopLeader Session - " + clientName,
                                sessionTime,
                                sessionTime.plusHours(1),
                                "topleader-session-" + sessionId);
                    })
                    .map(GoogleMeetApiClient.CalendarEventResponse::hangoutLink)
                    .filter(Objects::nonNull);
        }

        private void persistTokens(String username, String refreshToken, String accessToken) {
            Optional.ofNullable(refreshToken)
                    .ifPresent(rt -> repository.updateTokens(username, encryptor.encrypt(rt), encryptor.encrypt(accessToken)));
        }
    }

    private record ZoomMeetLinkGenerator(
            ZoomApiClient zoomApiClient,
            MeetingInfoRepository repository,
            TokenEncryptor encryptor
    ) implements MeetLinkGenerator {

        @Override
        public Optional<String> generate(MeetingInfo info, Long sessionId, LocalDateTime sessionTime, String clientName) {
            return Optional.ofNullable(zoomApiClient.refreshToken(encryptor.decrypt(info.getRefreshToken())))
                    .map(tokenResponse -> {
                        persistTokens(info.getUsername(), tokenResponse.refreshToken(), tokenResponse.accessToken());
                        return zoomApiClient.createMeeting(
                                tokenResponse.accessToken(),
                                "TopLeader Session - " + clientName,
                                60);
                    })
                    .map(ZoomApiClient.MeetingResponse::joinUrl)
                    .filter(Objects::nonNull);
        }

        private void persistTokens(String username, String refreshToken, String accessToken) {
            Optional.ofNullable(refreshToken)
                    .ifPresent(rt -> repository.updateTokens(username, encryptor.encrypt(rt), encryptor.encrypt(accessToken)));
        }
    }
}
