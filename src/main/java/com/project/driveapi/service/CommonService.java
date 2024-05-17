package com.project.driveapi.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.TimeZone;

@Service
public class CommonService {

    private final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private final Set<String> SCOPES = DriveScopes.all();
    private final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

    @Value("${google.secret key.path}")
    private String CREDENTIALS_FILE_PATH;

    @Value("${spring.application.name}")
    private String APPLICATION_NAME;

    @Value("${google.tokens.path}")
    private String TOKENS_DIRECTORY_PATH;

    @Value("${google.user.id}")
    private String USER_IDENTIFIER_KEY;

    public CommonService() throws GeneralSecurityException, IOException {
    }

    protected GoogleAuthorizationCodeFlow getFlow() throws IOException {
        InputStream in = FileService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        return new GoogleAuthorizationCodeFlow
                .Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
    }

    protected Drive getDrive() throws IOException {
        Credential credentials = getFlow().loadCredential(USER_IDENTIFIER_KEY);
        return new Drive
                .Builder(HTTP_TRANSPORT, JSON_FACTORY, credentials)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    protected LocalDateTime unixToLocalDateTime(Long unixTime) {
        return LocalDateTime
                .ofInstant(Instant
                        .ofEpochMilli(unixTime), TimeZone.getDefault().toZoneId());
    }
}
