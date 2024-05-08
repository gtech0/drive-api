package com.project.driveapi.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.FileUtils;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
public class DriveService {
    private final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
    private final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    private final Tika tika;

    @Value("${google.oauth.callback.uri}")
    private String CALLBACK_URI;

    @Value("${google.secret key.path}")
    private String CREDENTIALS_FILE_PATH;

    @Value("${spring.application.name}")
    private String APPLICATION_NAME;

    @Value("${google.tokens.path}")
    private String TOKENS_DIRECTORY_PATH;

    @Value("${google.user.id}")
    private String USER_IDENTIFIER_KEY;

    public DriveService(Tika tika) throws GeneralSecurityException, IOException {
        this.tika = tika;
    }

    public GoogleAuthorizationCodeFlow getFlow() throws IOException {
        InputStream in = DriveService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
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

    public void googleSignIn(HttpServletResponse response, GoogleAuthorizationCodeFlow flow) throws Exception {
        GoogleAuthorizationCodeRequestUrl url = flow.newAuthorizationUrl();
        String redirectURL = url.setRedirectUri(CALLBACK_URI).setAccessType("offline").build();
        response.sendRedirect(redirectURL);
    }

    public String saveAuthorizationCode(HttpServletRequest request, GoogleAuthorizationCodeFlow flow) throws Exception {
        String code = request.getParameter("code");
        if (code != null) {
            saveToken(code, flow);

            return "successfully logged in";
        }

        return "already logged in";
    }

    private void saveToken(String code, GoogleAuthorizationCodeFlow flow) throws Exception {
        GoogleTokenResponse response = flow.newTokenRequest(code).setRedirectUri(CALLBACK_URI).execute();
        flow.createAndStoreCredential(response, USER_IDENTIFIER_KEY);
    }

    public List<String> uploadFiles(GoogleAuthorizationCodeFlow flow, List<MultipartFile> multipartFiles) throws Exception {
        Credential credentials = flow.loadCredential(USER_IDENTIFIER_KEY);
        Drive drive = new Drive
                .Builder(HTTP_TRANSPORT, JSON_FACTORY, credentials)
                .setApplicationName(APPLICATION_NAME)
                .build();

        List<String> uploadedFilesIds = new ArrayList<>();
        for (MultipartFile multipartFile : multipartFiles) {
            java.io.File file = multipartFileToFile(multipartFile);

            String fileName = multipartFile.getOriginalFilename();
            String fileMimeType = getMimeType(fileName);

            FileContent content = new FileContent(fileMimeType, file);

            File googleFile = new File();
            googleFile.setName(fileName);
            googleFile.setMimeType(fileMimeType);

            File uploadedFile = drive
                    .files()
                    .create(googleFile, content)
                    .setFields("id")
                    .execute();
            uploadedFilesIds.add(String.format("fileID: '%s'", uploadedFile.getId()));

            FileUtils.forceDelete(file);
        }

        return uploadedFilesIds;
    }

    private java.io.File multipartFileToFile(MultipartFile multipartFile) throws IOException {
        java.io.File convFile = new java.io.File(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(multipartFile.getBytes());
        fos.close();

        return convFile;
    }

    private String getMimeType(String fileName) {
        String extensionType = tika.detect(fileName);
        System.out.println(extensionType);
        return extensionType;
    }
}
