package com.project.driveapi.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.services.drive.model.About;
import com.project.driveapi.dto.AboutDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class UserService {

    @Value("${google.oauth.callback.uri}")
    private String CALLBACK_URI;

    @Value("${google.user.id}")
    private String USER_IDENTIFIER_KEY;

    private final CommonService commonService;

    public void googleSignIn(HttpServletResponse response) throws Exception {
        GoogleAuthorizationCodeRequestUrl url = commonService.getFlow().newAuthorizationUrl();
        String redirectURL = url.setRedirectUri(CALLBACK_URI).setAccessType("offline").build();
        response.sendRedirect(redirectURL);
    }

    public void saveAuthorizationCode(HttpServletRequest request) throws Exception {
        String code = request.getParameter("code");
        if (code != null) {
            GoogleAuthorizationCodeFlow flow = commonService.getFlow();
            GoogleTokenResponse response = flow.newTokenRequest(code).setRedirectUri(CALLBACK_URI).execute();
            flow.createAndStoreCredential(response, USER_IDENTIFIER_KEY);
        }
    }

    public AboutDto getAboutInfo() throws IOException {
        About about = commonService.getDrive()
                .about()
                .get()
                .setFields("storageQuota,user")
                .execute();

        return AboutDto.builder()
                .emailAddress(about.getUser().getEmailAddress())
                .displayName(about.getUser().getDisplayName())
                .photoLink(about.getUser().getPhotoLink())
                .storageLimit(about.getStorageQuota().getLimit())
                .storageUsageInDrive(about.getStorageQuota().getUsageInDrive())
                .storageUsageInDriveTrash(about.getStorageQuota().getUsageInDriveTrash())
                .build();
    }
}
