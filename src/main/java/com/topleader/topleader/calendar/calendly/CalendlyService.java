package com.topleader.topleader.calendar.calendly;

import com.fasterxml.jackson.databind.JsonNode;
import com.topleader.topleader.calendar.CalendarSyncInfoRepository;
import com.topleader.topleader.calendar.CalendarToErrorHandler;
import com.topleader.topleader.calendar.calendly.domain.CalendlySchedules;
import com.topleader.topleader.calendar.domain.CalendarSyncInfo;
import com.topleader.topleader.calendar.domain.SyncEvent;
import com.topleader.topleader.calendar.calendly.domain.CalendlyUserInfo;
import com.topleader.topleader.calendar.calendly.domain.TokenResponse;

import com.topleader.topleader.coach.availability.CoachAvailabilityController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

import java.nio.charset.Charset;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


import static com.topleader.topleader.calendar.domain.CalendarSyncInfo.SyncType.CALENDLY;
import static com.topleader.topleader.util.common.user.UserUtils.getUserCalendlyUuid;
import static org.springframework.http.HttpHeaders.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalendlyService {

    private static final String SCHEDULE_URI_REPLACEMENT = "https://api.calendly.com/user_availability_schedules/";

    private final CalendarSyncInfoRepository repository;

    private final CalendlyProperties properties;

    private final RestClient restClient;

    private final CalendarToErrorHandler errorHandler;

    public void saveInfo(CalendarSyncInfo info) {
        log.info("Saving Calendly info: {}", info.getId().getUsername());
        repository.save(info);
    }

    public Optional<CalendarSyncInfo> findInfo(String email) {
        return repository.findByEmailOrUsername(email, CALENDLY);
    }

    public TokenResponse fetchTokens(String authorizationCode) {
        log.info("Fetching Calendly tokens");
        var body = new LinkedMultiValueMap<String, String>();
        body.add("grant_type", "authorization_code");
        body.add("code", authorizationCode);
        body.add("redirect_uri", properties.getRedirectUri().concat("?username=" + username));

        return restClient.post().uri(properties.getBaseAuthUrl().concat("/oauth/token"))
                .header(AUTHORIZATION, basic())
                .body(body)
                .retrieve()
                .body(TokenResponse.class);
    }

    public TokenResponse refreshTokens(CalendarSyncInfo info) {
        log.info("Refreshing Calendly token");
        var body = new LinkedMultiValueMap<String, String>();
        body.add("grant_type", "refresh_token");
        body.add("refresh_token", info.getRefreshToken());
        body.add("redirect_uri", properties.getRedirectUri());

        return restClient.post().uri(properties.getBaseAuthUrl().concat("/oauth/token"))
                .header(AUTHORIZATION, basic())
                .body(body)
                .retrieve()
                .body(TokenResponse.class);
    }


    public List<SyncEvent> getUserEvents(String username, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Looking for Calendly info");
        return repository.findById(new CalendarSyncInfo.CalendarInfoId(username, CalendarSyncInfo.SyncType.CALENDLY))
                .map(info -> {
                    log.info("Fetching Calendly user events");
                    try {
                        var response = restClient.get().uri(properties.getBaseApiUrl().concat("/scheduled_events?user={user}&start_time={start_time}&end_time={end_time}"),
                                        info.getOwnerUrl(),
                                        toCalendlyFormat(startDate),
                                        toCalendlyFormat(endDate))
                                .header(AUTHORIZATION, bearer(info.getAccessToken()))
                                .retrieve()
                                .body(JsonNode.class);

                        var events = new ArrayList<SyncEvent>();
                        response.get("collection").forEach(node ->
                                events.add(new SyncEvent(username, fromCalendlyFormat(node.get("start_time").asText()), fromCalendlyFormat(node.get("end_time").asText())))
                        );

                        return events;
                    } catch (Exception e) {
                        errorHandler.handleError(info, e);
                        return new ArrayList<SyncEvent>();
                    }
                })
                .orElse(new ArrayList<>());
    }

    public List<CalendlySchedules> getSchedules(String username) {
        log.info("Fetching Calendly events types");
        return repository.findById(new CalendarSyncInfo.CalendarInfoId(username, CALENDLY))
                .map(info -> {
                    try {
                        var response = restClient.get().uri(properties.getBaseApiUrl().concat("/user_availability_schedules?user={user}"),
                                        info.getOwnerUrl())
                                .header(AUTHORIZATION, bearer(info.getAccessToken()))
                                .retrieve()
                                .body(JsonNode.class);

                        var events = new ArrayList<CalendlySchedules>();
                        response.get("collection").forEach(node -> {
                                    var uri = node.get("uri").asText();
                                    var scheduleUuid = uri.replace(SCHEDULE_URI_REPLACEMENT, StringUtils.EMPTY);
                                    events.add(new CalendlySchedules(node.get("name").asText(), scheduleUuid));
                                }

                        );
                        return events;
                    } catch (Exception e) {
                        log.error("Error fetching Calendly schedules", e);
                        throw new RuntimeException("Error fetching Calendly schedules", e);
                    }
                })
                .orElse(new ArrayList<>());
    }


    public List<CoachAvailabilityController.ReoccurringEventDto> getScheduledAvailability(String username, String scheduleUuid) {
        log.info("Fetching Calendly scheduled availability");
        return repository.findById(new CalendarSyncInfo.CalendarInfoId(username, CALENDLY))
                .map(info -> {
                    try {
                        var response = restClient.get().uri(properties.getBaseApiUrl().concat("/user_availability_schedules/{scheduleUuid}"),
                                        info.getOwnerUrl(), scheduleUuid)
                                .header(AUTHORIZATION, bearer(info.getAccessToken()))
                                .retrieve()
                                .body(JsonNode.class);

                        var events = new ArrayList<CoachAvailabilityController.ReoccurringEventDto>();
                        response.get("resource").get("rules").forEach(node -> {
                            var dayOfTheWeek = getDayOfWeek(node.get("wday").asText());
                            node.get("intervals").forEach(interval -> {
                                var dto = new CoachAvailabilityController.ReoccurringEventDto(from(dayOfTheWeek, interval), to(dayOfTheWeek, interval));
                                events.add(dto);
                            });
                        });
                        return events;
                    } catch (Exception e) {
                        log.error("Error fetching scheduled availability", e);
                        throw new RuntimeException("Error fetching scheduled availability", e);
                    }
                })
                .orElse(new ArrayList<>());
    }

    private DayOfWeek getDayOfWeek(String day) {
        return switch (day) {
            case "monday" -> DayOfWeek.MONDAY;
            case "tuesday" -> DayOfWeek.TUESDAY;
            case "wednesday" -> DayOfWeek.WEDNESDAY;
            case "thursday" -> DayOfWeek.THURSDAY;
            case "friday" -> DayOfWeek.FRIDAY;
            case "saturday" -> DayOfWeek.SATURDAY;
            case "sunday" -> DayOfWeek.SUNDAY;
            default -> throw new IllegalArgumentException("Invalid day of week: " + day);
        };
    }

    private CoachAvailabilityController.ReoccurringEventTimeDto from(DayOfWeek dayOfTheWeek, JsonNode interval) {
        return new CoachAvailabilityController.ReoccurringEventTimeDto(dayOfTheWeek, LocalTime.parse(interval.get("from").asText()));
    }

    private CoachAvailabilityController.ReoccurringEventTimeDto to(DayOfWeek dayOfTheWeek, JsonNode interval) {
        return new CoachAvailabilityController.ReoccurringEventTimeDto(dayOfTheWeek, LocalTime.parse(interval.get("to").asText()));
    }

    private String basic() {
        return "Basic " + encodeBasicAuth(properties.getClientId(), properties.getClientSecrets(), Charset.defaultCharset());
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    public static String toCalendlyFormat(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.BASIC_ISO_DATE);
    }

    public static LocalDateTime fromCalendlyFormat(String dateTime) {
        return LocalDateTime.parse(dateTime, DateTimeFormatter.ISO_ZONED_DATE_TIME);
    }
}
