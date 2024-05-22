package com.project.driveapi.component;

import com.google.api.client.http.FileContent;
import com.project.driveapi.exception.ControllerErrorHandler;
import com.project.driveapi.exception.DuplicateFileException;
import com.project.driveapi.exception.NotFoundException;
import com.project.driveapi.service.CommonService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
public class CustomFileAlterationListener extends FileAlterationListenerAdaptor {

    private final Path path;
    private final CommonService commonService;
    private final ControllerErrorHandler errorHandler;

    @Override
    public void onDirectoryCreate(File directory) {
        try {
            Path folderPath = path.normalize().relativize(directory.toPath()).getFileName();
            Path parentPath = Path.of(directory.getCanonicalPath()).getParent();

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

            parentId = createParentIfNotExists(parentId, parentPath);

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
            errorHandler.handleUnknownException(e);
        }
    }

    @SneakyThrows
    @Override
    public void onDirectoryDelete(File directory) {
        try {
            Path folderPath = path.normalize().relativize(directory.toPath()).getFileName();

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
            errorHandler.handleUnknownException(e);
        }
    }

    @SneakyThrows
    @Override
    public void onFileCreate(File file) {
        try {
            Path filePath = path.normalize().relativize(file.toPath()).getFileName();
            Path parentPath = Path.of(file.getCanonicalPath()).getParent();

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

            String parentId = checkForParent(files, parentPath);

            for (com.google.api.services.drive.model.File googlefile : files) {
                if (googlefile.getParents().contains(parentId)
                        && Objects.equals(googlefile.getName(), filePath.toString())
                        && Objects.equals(googlefile.getMd5Checksum(), md5Checksum)
                        && !googlefile.getTrashed()
                ) {
                    throw new DuplicateFileException("Duplicate file");
                }
            }

            parentId = createParentIfNotExists(parentId, parentPath);

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
            errorHandler.handleUnknownException(e);
        }
    }

    private String createParentIfNotExists(String parentId, Path parentPath) throws IOException {
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
        return parentId;
    }

    @SneakyThrows
    @Override
    public void onFileChange(File file) {
        try {
            Path filePath = path.normalize().relativize(file.toPath()).getFileName();
            Path parentPath = Path.of(file.getCanonicalPath()).getParent();

            List<com.google.api.services.drive.model.File> files = commonService.getDrive()
                    .files()
                    .list()
                    .setFields("files(id,name,mimeType,trashed,md5Checksum,parents)")
                    .execute()
                    .getFiles();

            String parentId = checkForParent(files, parentPath);
            String fileId = checkForFile(files, parentId, filePath);

            String fileName = filePath.toString();
            String fileMimeType = commonService.getMimeType(fileName);

            FileContent content = new FileContent(fileMimeType, file);

            com.google.api.services.drive.model.File googleFile = new com.google.api.services.drive.model.File();
            googleFile.setName(fileName);

            commonService.getDrive()
                    .files()
                    .update(fileId, googleFile, content)
                    .setFields("id")
                    .execute();
        } catch (Exception e) {
            errorHandler.handleUnknownException(e);
        }
    }

    @SneakyThrows
    @Override
    public void onFileDelete(File file) {
        try {
            Path filePath = path.normalize().relativize(file.toPath()).getFileName();
            Path parentPath = Path.of(file.getCanonicalPath()).getParent();

            List<com.google.api.services.drive.model.File> files = commonService.getDrive()
                    .files()
                    .list()
                    .setFields("files(id,name,mimeType,trashed,md5Checksum,parents)")
                    .execute()
                    .getFiles();

            String parentId = checkForParent(files, parentPath);
            String fileId = checkForFile(files, parentId, filePath);

            commonService.getDrive()
                    .files()
                    .delete(fileId)
                    .setFields("id")
                    .execute();
        } catch (Exception e) {
            errorHandler.handleUnknownException(e);
        }
    }

    private static String checkForFile(List<com.google.api.services.drive.model.File> files, String parentId, Path filePath) {
        String fileId = null;
        for (com.google.api.services.drive.model.File googlefile : files) {
            if (googlefile.getParents().contains(parentId)
                    && Objects.equals(googlefile.getName(), filePath.toString())
                    && !googlefile.getTrashed()
            ) {
                fileId = googlefile.getId();
                break;
            }
        }

        if (fileId == null) {
            throw new NotFoundException("File not found");
        }
        return fileId;
    }

    private static String checkForParent(List<com.google.api.services.drive.model.File> files, Path parentPath) {
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
        return parentId;
    }
}
