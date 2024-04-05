/*
 * Copyright (c) 2024 Price f(x), s.r.o.
 */
package com.topleader.topleader.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.CalendarScopes;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @author Daniel Slavik
 */
@Configuration
public class GoogleClientConfiguration {

    @Bean
    public GoogleAuthorizationCodeFlow getAuthorizationFlow(
        @Value("${google.client.client-id}") String clientId,
        @Value("${google.client.client-secret}") String clientSecret
    ) throws GeneralSecurityException, IOException {
        return new GoogleAuthorizationCodeFlow
            .Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            GsonFactory.getDefaultInstance(),
            new GoogleClientSecrets().setWeb(
                new GoogleClientSecrets.Details()
                    .setClientId(clientId)
                    .setClientSecret(clientSecret)
            ),
            Set.of(CalendarScopes.CALENDAR_READONLY, CalendarScopes.CALENDAR_EVENTS_READONLY)
        ).build();
    }
}
