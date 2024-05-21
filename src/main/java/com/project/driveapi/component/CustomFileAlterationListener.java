package com.project.driveapi.component;

import com.google.api.client.http.FileContent;
import com.project.driveapi.exception.DuplicateFileException;
import com.project.driveapi.exception.NotFoundException;
import com.project.driveapi.service.CommonService;
import lombok.SneakyThrows;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class CustomFileAlterationListener extends FileAlterationListenerAdaptor {
    private final Path path;
    private final CommonService commonService;

    public CustomFileAlterationListener(Path path, CommonService commonService) {
        this.path = path;
        this.commonService = commonService;
    }

    @Override
    public void onDirectoryCreate(File directory) {
        try {
            Path folderPath = path.normalize().relativize(directory.toPath()).getFileName();
            Path parentPath = Path.of(directory.getCanonicalPath()).getParent();

            System.out.println("Directory created: " + directory.getCanonicalPath());
            System.out.println("Directory root name: " + path.getFileName());
            System.out.println("Directory name: " + folderPath);
            System.out.println("Directory parent: " + Path.of(directory.getCanonicalPath()).getParent());

            List<com.google.api.services.drive.model.File> files = commonService.getDrive()
                    .files()
                    .list()
                    .setFields("files(id,name,mimeType,trashed)")
                    .execute()
                    .getFiles();

            String parentId = null;
            for (com.google.api.services.drive.model.File file : files) {
                if (Objects.equals(file.getMimeType(), "application/vnd.google-apps.folder")
                        && Objects.equals(file.getName(), parentPath.getFileName().toString())
                        && !file.getTrashed()
                ) {
                    parentId = file.getId();
                }
            }

            if (parentId == null && path.getFileName().toString().equals(parentPath.getFileName().toString())) {
                com.google.api.services.drive.model.File googleFile = new com.google.api.services.drive.model.File();
                googleFile.setName(path.getFileName().toString());
                googleFile.setMimeType("application/vnd.google-apps.folder");

                parentId = commonService.getDrive()
                        .files()
                        .create(googleFile)
                        .setFields("id")
                        .execute()
                        .getId();
                System.out.println(parentId);
            } else if (parentId == null) {
                throw new NotFoundException("Parent folder not found");
            }

            com.google.api.services.drive.model.File googleFile = new com.google.api.services.drive.model.File();
            googleFile.setName(folderPath.toString());
            googleFile.setMimeType("application/vnd.google-apps.folder");
            googleFile.setParents(Collections.singletonList(parentId));

            commonService.getDrive()
                    .files()
                    .create(googleFile)
                    .setFields("id")
                    .execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    @Override
    public void onDirectoryChange(File directory) {
        System.out.println("Directory changed: " + directory.getCanonicalPath());
    }

    @SneakyThrows
    @Override
    public void onDirectoryDelete(File directory) {
        try {
            Path folderPath = path.normalize().relativize(directory.toPath()).getFileName();

            System.out.println("Directory deleted: " + directory.getCanonicalPath());
            System.out.println("Directory root name: " + path.getFileName());
            System.out.println("Directory name: " + folderPath);
            System.out.println("Directory parent: " + Path.of(directory.getCanonicalPath()).getParent());

            List<com.google.api.services.drive.model.File> files = commonService.getDrive()
                    .files()
                    .list()
                    .setFields("files(id,name,mimeType,trashed)")
                    .execute()
                    .getFiles();

            String dirId = null;
            for (com.google.api.services.drive.model.File file : files) {
                if (Objects.equals(file.getMimeType(), "application/vnd.google-apps.folder")
                        && Objects.equals(file.getName(), folderPath.toString())
                        && !file.getTrashed()
                ) {
                    dirId = file.getId();
                }
            }

            if (dirId == null) {
                throw new NotFoundException("Folder not found");
            }

            commonService.getDrive()
                    .files()
                    .delete(dirId)
                    .setFields("id")
                    .execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    @Override
    public void onFileCreate(File file) {
        try {
            Path filePath = path.normalize().relativize(file.toPath()).getFileName();
            Path parentPath = Path.of(file.getCanonicalPath()).getParent();

            System.out.println("File created: " + file.getCanonicalPath());
            System.out.println("File root name: " + path.getFileName());
            System.out.println("File name: " + filePath);
            System.out.println("File parent: " + Path.of(file.getCanonicalPath()).getParent());

            List<com.google.api.services.drive.model.File> files = commonService.getDrive()
                    .files()
                    .list()
                    .setFields("files(id,name,mimeType,trashed,md5Checksum,parents)")
                    .execute()
                    .getFiles();

            String md5Checksum;
            try (InputStream is = Files.newInputStream(file.toPath())) {
                md5Checksum = DigestUtils.md5Hex(is);
            }

            String parentId = null;
            for (com.google.api.services.drive.model.File googlefile : files) {
                if (Objects.equals(googlefile.getMimeType(), "application/vnd.google-apps.folder")
                        && Objects.equals(googlefile.getName(), parentPath.getFileName().toString())
                        && !googlefile.getTrashed()
                ) {
                    parentId = googlefile.getId();
                    break;
                }
            }

            for (com.google.api.services.drive.model.File googlefile : files) {
                if (googlefile.getParents().contains(parentId)
                        && Objects.equals(googlefile.getName(), file.toPath().getFileName().toString())
                        && Objects.equals(googlefile.getMd5Checksum(), md5Checksum)
                        && !googlefile.getTrashed()
                ) {
                    throw new DuplicateFileException("Duplicate file");
                }
            }

            if (parentId == null && path.getFileName().toString().equals(parentPath.getFileName().toString())) {
                com.google.api.services.drive.model.File googleFile = new com.google.api.services.drive.model.File();
                googleFile.setName(path.getFileName().toString());
                googleFile.setMimeType("application/vnd.google-apps.folder");

                parentId = commonService.getDrive()
                        .files()
                        .create(googleFile)
                        .setFields("id")
                        .execute()
                        .getId();
            } else if (parentId == null) {
                throw new NotFoundException("Parent folder not found");
            }

            String fileName = filePath.toString();
            String fileMimeType = commonService.getMimeType(fileName);

            FileContent content = new FileContent(fileMimeType, file);

            com.google.api.services.drive.model.File googleFile = new com.google.api.services.drive.model.File();
            googleFile.setName(fileName);
            googleFile.setParents(Collections.singletonList(parentId));

            commonService.getDrive()
                    .files()
                    .create(googleFile, content)
                    .setFields("id")
                    .execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    @Override
    public void onFileChange(File file) {
        System.out.println("File changed: " + file.getCanonicalPath());
    }

    @SneakyThrows
    @Override
    public void onFileDelete(File file) {
        try {
            Path filePath = path.normalize().relativize(file.toPath()).getFileName();
            Path parentPath = Path.of(file.getCanonicalPath()).getParent();

            System.out.println("File deleted: " + file.getCanonicalPath());
            System.out.println("File root name: " + path.getFileName());
            System.out.println("File name: " + filePath);
            System.out.println("File parent: " + Path.of(file.getCanonicalPath()).getParent());

            List<com.google.api.services.drive.model.File> files = commonService.getDrive()
                    .files()
                    .list()
                    .setFields("files(id,name,mimeType,trashed,md5Checksum,parents)")
                    .execute()
                    .getFiles();

            String parentId = null;
            for (com.google.api.services.drive.model.File googlefile : files) {
                if (Objects.equals(googlefile.getMimeType(), "application/vnd.google-apps.folder")
                        && Objects.equals(googlefile.getName(), parentPath.getFileName().toString())
                        && !googlefile.getTrashed()
                ) {
                    parentId = googlefile.getId();
                    break;
                }
            }

            String fileId = null;
            for (com.google.api.services.drive.model.File googlefile : files) {
                if (googlefile.getParents().contains(parentId)
                        && Objects.equals(googlefile.getName(), file.toPath().getFileName().toString())
                        && !googlefile.getTrashed()
                ) {
                    fileId = googlefile.getId();
                    break;
                }
            }

            if (fileId == null) {
                throw new NotFoundException("File not found");
            }

            commonService.getDrive()
                    .files()
                    .delete(fileId)
                    .setFields("id")
                    .execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
