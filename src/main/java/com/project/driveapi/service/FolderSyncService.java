package com.project.driveapi.service;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import static java.nio.file.StandardWatchEventKinds.*;

@Component
public class FolderSyncService {

    private final CommonService commonService;
    private final WatchService watchService = FileSystems.getDefault().newWatchService();

    public FolderSyncService(CommonService commonService) throws IOException {
        this.commonService = commonService;
    }

    private void registerRecursive(List<Path> paths) throws IOException {
        for (Path path : paths) {
            Files.walkFileTree(path, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    dir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    public void watch() throws IOException, InterruptedException {
        //registerRecursive(Path.of("testDir"));
        commonService.getFlow().getClientId();

        WatchKey key;
        while ((key = watchService.take()) != null) {
            for (WatchEvent<?> event : key.pollEvents()) {
                System.out.println("Event kind:" + event.kind()
                                + ". File affected: "
                                + event.context() + ".");
            }
            key.reset();
        }
    }

}
