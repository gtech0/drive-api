package com.project.driveapi.component;

import lombok.SneakyThrows;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;

import java.io.File;
import java.nio.file.Path;

public class CustomFileAlterationListener extends FileAlterationListenerAdaptor {
    private final Path path;

    public CustomFileAlterationListener(Path path) {
        this.path = path;
    }

    @SneakyThrows
    @Override
    public void onDirectoryCreate(File directory) {
        System.out.println("Directory created: " + directory.getCanonicalPath());
        System.out.println("Relative path: " + path.normalize().relativize(directory.toPath()));
        System.out.println("Filename: " + path.getFileName());
    }

    @SneakyThrows
    @Override
    public void onDirectoryChange(File directory) {
        System.out.println("Directory changed: " + directory.getCanonicalPath());
    }

    @SneakyThrows
    @Override
    public void onDirectoryDelete(File directory) {
        System.out.println("Directory deleted: " + directory.getCanonicalPath());
    }

    @SneakyThrows
    @Override
    public void onFileCreate(File file) {
        System.out.println("File created: " + file.getCanonicalPath());
    }

    @SneakyThrows
    @Override
    public void onFileChange(File file) {
        System.out.println("File changed: " + file.getCanonicalPath());
    }

    @SneakyThrows
    @Override
    public void onFileDelete(File file) {
        System.out.println("File removed: " + file.getCanonicalPath());
    }
}
