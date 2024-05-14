package com.project.driveapi.service;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.project.driveapi.dto.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FileService {
    private final Tika tika = new Tika();

    private final CommonService commonService;

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

            File uploadedFile = commonService.getDrive()
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
            commonService.getDrive()
                    .files()
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

        File uploadedFile = commonService.getDrive()
                .files()
                .create(googleFile)
                .setFields("id")
                .execute();
        return String.format("fileID: '%s'", uploadedFile.getId());
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
