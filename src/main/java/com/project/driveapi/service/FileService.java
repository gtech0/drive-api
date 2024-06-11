package com.project.driveapi.service;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.project.driveapi.dto.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FileService {
    private final CommonService commonService;

    public void uploadFiles(List<MultipartFile> multipartFiles,
                            String targetFolderId) throws Exception {
        for (MultipartFile multipartFile : multipartFiles) {
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
    }

    public void downloadFiles(Map<String, String> download) throws Exception {
        for (Map.Entry<String, String> file : download.entrySet()) {
            OutputStream fos = new FileOutputStream(file.getValue());
            commonService.getDrive()
                    .files()
                    .get(file.getKey())
                    .setAlt("media")
                    .executeMediaAndDownloadTo(fos);
            fos.close();
        }
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

    public List<GoogleFileDto> listFiles() throws Exception {
        List<GoogleFileDto> files = new ArrayList<>();
        FileList googleFiles = commonService.getDrive()
                .files()
                .list()
                .setFields("files(id,name,mimeType,createdTime,modifiedTime,permissions,trashed,size,parents)")
                .execute();

        for (File googleFile : googleFiles.getFiles()) {
            files.add(GoogleFileDto.builder()
                    .id(googleFile.getId())
                    .name(googleFile.getName())
                    .mimeType(googleFile.getMimeType())
                    .createdTime(commonService.unixToLocalDateTime(googleFile.getCreatedTime().getValue()))
                    .modifiedTime(commonService.unixToLocalDateTime(googleFile.getModifiedTime().getValue()))
                    .permissions(mapPermissions(googleFile))
                    .trashed(googleFile.getTrashed())
                    .size(googleFile.getSize())
                    .parents(googleFile.getParents())
                    .build());
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
                .execute();

        return GoogleFileShortDto.builder()
                .id(googleFile.getId())
                .name(googleFile.getName())
                .mimeType(googleFile.getMimeType())
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
