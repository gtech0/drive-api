package com.project.driveapi.service;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import com.google.api.services.drive.model.User;
import com.project.driveapi.dto.*;
import com.project.driveapi.exception.FileNotExportableException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FileService {
    private final CommonService commonService;

    public void uploadFile(MultipartFile multipartFile, String targetFolderId) throws Exception {
        java.io.File file = multipartFileToFile(multipartFile);

        String fileName = multipartFile.getOriginalFilename();
        String fileMimeType = commonService.getMimeType(fileName);

        FileContent content = new FileContent(fileMimeType, file);

        File googleFile = new File();
        googleFile.setName(fileName);
        if (targetFolderId != null) {
            googleFile.setParents(Collections.singletonList(targetFolderId));
        }

        commonService.getDrive()
                .files()
                .create(googleFile, content)
                .setFields("id")
                .execute();

        FileUtils.forceDelete(file);
    }

    public ResponseEntity<Resource> downloadFile(String fileId) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GoogleFileShortDto file = getFile(fileId);

        List<String> googleMimeTypes = List.of(
                "application/vnd.google-apps.document",
                "application/vnd.google-apps.presentation",
                "application/vnd.google-apps.spreadsheet",
                "application/vnd.google-apps.drawing",
                "application/vnd.google-apps.script"
        );

        Pair<String, String> data = new ImmutablePair<>(file.getMimeType(), "");
        if (googleMimeTypes.contains(data.getLeft())) {
            data = getGoogleMimeTypeAndExtension(data.getLeft());
            if (Objects.equals(data.getRight(), "")) {
                throw new FileNotExportableException("Something went wrong: mimeType is empty");
            }

            commonService.getDrive()
                    .files()
                    .export(fileId, data.getLeft())
                    .setAlt("media")
                    .executeMediaAndDownloadTo(baos);
        } else {
            commonService.getDrive()
                    .files()
                    .get(fileId)
                    .setAlt("media")
                    .executeMediaAndDownloadTo(baos);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=" + URLEncoder.encode(file.getName(), StandardCharsets.UTF_8) + data.getRight()
        );

        ByteArrayResource resource = new ByteArrayResource(baos.toByteArray());
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(baos.size())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    private Pair<String, String> getGoogleMimeTypeAndExtension(String mimeType) {
        return switch (mimeType) {
            case "application/vnd.google-apps.document" ->
                    new ImmutablePair<>("application/vnd.openxmlformats-officedocument.wordprocessingml.document", ".docx");
            case "application/vnd.google-apps.presentation" ->
                    new ImmutablePair<>("application/vnd.openxmlformats-officedocument.presentationml.presentation", ".pptx");
            case "application/vnd.google-apps.spreadsheet" ->
                    new ImmutablePair<>("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", ".xlsx");
            case "application/vnd.google-apps.drawing" -> new ImmutablePair<>("image/png", ".png");
            case "application/vnd.google-apps.script" ->
                    new ImmutablePair<>("application/vnd.google-apps.script+json", ".json");
            default -> new ImmutablePair<>(mimeType, "");
        };
    }

    public void createFolder(FolderDto folder) throws Exception {
        File googleFile = new File();
        googleFile.setName(folder.getFolderName());
        googleFile.setMimeType("application/vnd.google-apps.folder");
        if (folder.getTargetFolderId() != null) {
            googleFile.setParents(Collections.singletonList(folder.getTargetFolderId()));
        }

        commonService.getDrive()
                .files()
                .create(googleFile)
                .setFields("id")
                .execute();
    }

    public void updateFile(MultipartFile multipartFile, String fileId) throws IOException {
        java.io.File file = multipartFileToFile(multipartFile);

        String fileName = multipartFile.getOriginalFilename();
        String fileMimeType = commonService.getMimeType(fileName);

        FileContent content = new FileContent(fileMimeType, file);

        File googleFile = new File();
        googleFile.setName(fileName);

        commonService.getDrive()
                .files()
                .update(fileId, googleFile, content)
                .execute();
    }

    public List<GoogleFileDto> listFiles(String isTrashed) throws Exception {
        List<GoogleFileDto> files = new ArrayList<>();
        FileList googleFiles = commonService.getDrive()
                .files()
                .list()
                .setFields("files(id,name,mimeType,createdTime,modifiedTime,permissions,trashed,size,parents,capabilities)")
                .execute();

        List<String> trashed = List.of("true", "false");
        for (File googleFile : googleFiles.getFiles()) {
            if (Objects.equals(isTrashed, "true") && googleFile.getTrashed()
                    || Objects.equals(isTrashed, "false") && !googleFile.getTrashed()
                    || !trashed.contains(isTrashed)) {
                String currentRole = null;

                if (googleFile.getPermissions() != null) {
                    for (Permission permission : googleFile.getPermissions()) {
                        if (!Objects.equals(permission.getId(), "anyoneWithLink")) {
                            User user = commonService.getDrive()
                                    .about()
                                    .get()
                                    .setFields("user")
                                    .execute()
                                    .getUser();

                            if (Objects.equals(permission.getEmailAddress(), user.getEmailAddress())) {
                                currentRole = permission.getRole();
                            }
                        } else if (currentRole == null && Objects.equals(permission.getId(), "anyoneWithLink")) {
                            currentRole = permission.getRole();
                        }
                    }
                } else {
                    if (googleFile.getCapabilities().getCanComment()) {
                        currentRole = "commenter";
                    } else {
                        currentRole = "reader";
                    }
                }

                GoogleFileShortDto file = getFile("root");
                if (googleFile.getParents() == null && !Objects.equals(currentRole, "owner")) {
                    googleFile.setParents(List.of(file.getId()));
                }

                files.add(GoogleFileDto.builder()
                        .id(googleFile.getId())
                        .name(googleFile.getName())
                        .mimeType(googleFile.getMimeType())
                        .createdTime(commonService.unixToLocalDateTime(googleFile.getCreatedTime().getValue()))
                        .modifiedTime(commonService.unixToLocalDateTime(googleFile.getModifiedTime().getValue()))
                        .myRole(currentRole)
                        .permissions(mapPermissions(googleFile))
                        .trashed(googleFile.getTrashed())
                        .size(googleFile.getSize())
                        .parents(googleFile.getParents())
                        .build());
            }
        }
        return files;
    }

    private static List<Object> mapPermissions(File googleFile) {
        if (googleFile.getPermissions() == null) {
            return null;
        }

        return googleFile.getPermissions()
                .stream()
                .map(
                        permission -> {
                            if (Objects.equals(permission.getId(), "anyoneWithLink")) {
                                return PermissionGetAnyoneDto.builder()
                                        .id(permission.getId())
                                        .role(permission.getRole())
                                        .type(permission.getType())
                                        .build();
                            } else {
                                return PermissionGetDto.builder()
                                        .id(permission.getId())
                                        .role(permission.getRole())
                                        .type(permission.getType())
                                        .displayName(permission.getDisplayName())
                                        .emailAddress(permission.getEmailAddress())
                                        .photoLink(permission.getPhotoLink())
                                        .build();
                            }
                        }
                ).toList();
    }

    public GoogleFileShortDto getFile(String fileId) throws Exception {
        File googleFile = commonService.getDrive()
                .files()
                .get(fileId)
                .setFields("id,name,mimeType,parents")
                .execute();

        File root = commonService.getDrive()
                .files()
                .get("root")
                .setFields("id")
                .execute();

        if (!Objects.equals(googleFile.getId(), root.getId()) && googleFile.getParents() == null) {
            googleFile.setParents(List.of(root.getId()));
        }

        return GoogleFileShortDto.builder()
                .id(googleFile.getId())
                .name(googleFile.getName())
                .mimeType(googleFile.getMimeType())
                .parents(googleFile.getParents())
                .build();
    }

    public void deleteFiles(List<String> files) throws Exception {
        for (String fileId : files) {
            commonService.getDrive()
                    .files()
                    .delete(fileId)
                    .execute();
        }
    }

    private java.io.File multipartFileToFile(MultipartFile multipartFile) throws IOException {
        java.io.File file = new java.io.File(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(multipartFile.getBytes());
        fos.close();

        return file;
    }
}
