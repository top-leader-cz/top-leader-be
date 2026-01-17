/*
 * Copyright (c) 2024 Price f(x), s.r.o.
 */
package com.topleader.topleader.common.calendar.google;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.UserCredentials;
import java.io.IOException;
import java.security.GeneralSecurityException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GoogleCalendarApiClientFactory {

    private final HttpTransport transport;
    private final GsonFactory jsonFactory;

    @Value("${google.application-name}")
    private String applicationName;

    @Value("${google.client.client-id}")
    private String clientId;

    @Value("${google.client.client-secret}")
    private String clientSecret;

    public GoogleCalendarApiClientFactory() throws GeneralSecurityException, IOException {
        this.transport = GoogleNetHttpTransport.newTrustedTransport();
        this.jsonFactory = GsonFactory.getDefaultInstance();
    }

    public Calendar prepareCalendarClient(String refreshToken) {
        var credentials = UserCredentials.newBuilder()
            .setClientId(clientId)
            .setClientSecret(clientSecret)
            .setRefreshToken(refreshToken)
            .build();

        return new Calendar.Builder(
            transport,
            jsonFactory,
            new HttpCredentialsAdapter(credentials))
            .setApplicationName(applicationName)
            .build();
    }

}
