/*
 * Copyright (c) 2024 Price f(x), s.r.o.
 */
package com.topleader.topleader.calendar.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import java.io.IOException;
import java.security.GeneralSecurityException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


/**
 * @author Daniel Slavik
 */
@Service
@RequiredArgsConstructor
public class GoogleCalendarApiClientFactory {

    private static final String TOKEN_SERVER_URL = "https://accounts.google.com/o/oauth2/token";

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

        return prepareCalendarClient(
            new GoogleCredential.Builder()
                .setTransport(transport)
                .setJsonFactory(jsonFactory)
                .setClientSecrets(clientId, clientSecret)
                .setTokenServerEncodedUrl(TOKEN_SERVER_URL)
                .build()
                .setRefreshToken(refreshToken)
        );
    }

    private Calendar prepareCalendarClient(HttpRequestInitializer credentials) {


        return new Calendar.Builder(
            transport,
            jsonFactory,
            credentials)
            .setApplicationName(applicationName)
            .build();
    }

}
