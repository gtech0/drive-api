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
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import com.project.driveapi.dto.*;
import com.project.driveapi.entity.PermissionTypeEnum;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.FileUtils;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class DriveService {
    private final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private final Set<String> SCOPES = DriveScopes.all();
    private final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    private final Tika tika = new Tika();

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

    public DriveService() throws GeneralSecurityException, IOException {
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

    public void googleSignIn(HttpServletResponse response) throws Exception {
        GoogleAuthorizationCodeRequestUrl url = getFlow().newAuthorizationUrl();
        String redirectURL = url.setRedirectUri(CALLBACK_URI).setAccessType("offline").build();
        response.sendRedirect(redirectURL);
    }

    public String saveAuthorizationCode(HttpServletRequest request) throws Exception {
        String code = request.getParameter("code");
        if (code != null) {
            GoogleAuthorizationCodeFlow flow = getFlow();
            GoogleTokenResponse response = flow.newTokenRequest(code).setRedirectUri(CALLBACK_URI).execute();
            flow.createAndStoreCredential(response, USER_IDENTIFIER_KEY);
        }

        return "logged in";
    }

    public List<String> uploadFiles(List<MultipartFile> multipartFiles,
                                    String targetFolderId) throws Exception {
        List<String> uploadedFilesIds = new ArrayList<>();
        for (MultipartFile multipartFile : multipartFiles) {
            java.io.File file = multipartFileToFile(multipartFile);

            String fileName = multipartFile.getOriginalFilename();
            String fileMimeType = getMimeType(fileName);

            FileContent content = new FileContent(fileMimeType, file);

            File googleFile = new File();
            googleFile.setName(fileName);
            if (targetFolderId != null) {
                googleFile.setParents(Collections.singletonList(targetFolderId));
            }

            File uploadedFile = getDrive()
                    .files()
                    .create(googleFile, content)
                    .setFields("id")
                    .execute();
            uploadedFilesIds.add(String.format("fileID: '%s'", uploadedFile.getId()));

            FileUtils.forceDelete(file);
        }

        return uploadedFilesIds;
    }

    public void downloadFiles(Map<String, String> download) throws Exception {
        for (Map.Entry<String, String> file : download.entrySet()) {
            OutputStream fos = new FileOutputStream(file.getValue());
            getDrive().files()
                    .get(file.getKey())
                    .executeMediaAndDownloadTo(fos);
            fos.close();
        }
    }

    public String createFolder(FolderDto folder) throws Exception {
        File googleFile = new File();
        googleFile.setName(folder.getFolderName());
        googleFile.setMimeType("application/vnd.google-apps.folder");
        if (folder.getTargetFolderId() != null) {
            googleFile.setParents(Collections.singletonList(folder.getTargetFolderId()));
        }

        File uploadedFile = getDrive()
                .files()
                .create(googleFile)
                .setFields("id")
                .execute();
        return String.format("fileID: '%s'", uploadedFile.getId());
    }

    public List<GoogleFileDto> listFiles() throws Exception {
        List<GoogleFileDto> files = new ArrayList<>();
        FileList googleFiles = getDrive()
                .files()
                .list()
                .setFields("files(id,name,mimeType,createdTime,modifiedTime,permissions,trashed,size,parents)")
                .execute();

        for (File googleFile : googleFiles.getFiles()) {
            files.add(GoogleFileDto.builder()
                    .id(googleFile.getId())
                    .name(googleFile.getName())
                    .mimeType(googleFile.getMimeType())
                    .createdTime(unixToLocalDateTime(googleFile.getCreatedTime().getValue()))
                    .modifiedTime(unixToLocalDateTime(googleFile.getModifiedTime().getValue()))
                    .permissions(googleFile.getPermissions())
                    .trashed(googleFile.getTrashed())
                    .size(googleFile.getSize())
                    .parents(googleFile.getParents())
                    .build());
        }
        return files;
    }

    public GoogleFileShortDto getFile(String fileId) throws Exception {
        File googleFile = getDrive()
                .files()
                .get(fileId)
                .execute();

        return GoogleFileShortDto.builder()
                .id(googleFile.getId())
                .name(googleFile.getName())
                .mimeType(googleFile.getMimeType())
                .build();
    }

    public void deleteFiles(List<String> files) throws Exception {
        for (String fileId : files) {
            getDrive().files().delete(fileId).execute();
        }
    }

    public void trashFiles(List<String> files) throws Exception {
        for (String fileId : files) {
            File googleFile = new File();
            googleFile.setTrashed(true);
            getDrive().files().update(fileId, googleFile).execute();
        }
    }

    public void untrashFiles(List<String> files) throws Exception {
        for (String fileId : files) {
            File googleFile = new File();
            googleFile.setTrashed(false);
            getDrive().files().update(fileId, googleFile).execute();
        }
    }

    public void emptyTrash() throws Exception {
        getDrive().files().emptyTrash().execute();
    }

    public void addPermission(PermissionDto permission, String fileId) throws Exception {
        Permission newPermission = new Permission();
        newPermission.setType(permission.getType().name());
        newPermission.setRole(permission.getRole().name());
        if (permission.getType() == PermissionTypeEnum.user || permission.getType() == PermissionTypeEnum.group) {
            newPermission.setEmailAddress(permission.getEmailAddressOrDomain());
        } else if (permission.getType() == PermissionTypeEnum.domain) {
            newPermission.setDomain(permission.getEmailAddressOrDomain());
        }

        getDrive().permissions().create(fileId, newPermission).execute();
    }

    public void revokePermission(String permissionId, String fileId) throws Exception {
        getDrive().permissions().delete(fileId, permissionId).execute();
    }

    private Drive getDrive() throws IOException {
        Credential credentials = getFlow().loadCredential(USER_IDENTIFIER_KEY);
        return new Drive
                .Builder(HTTP_TRANSPORT, JSON_FACTORY, credentials)
                .setApplicationName(APPLICATION_NAME)
                .build();
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

    private LocalDateTime unixToLocalDateTime(Long unixTime) {
        return LocalDateTime
                .ofInstant(Instant
                        .ofEpochMilli(unixTime), TimeZone.getDefault().toZoneId());
    }
}
