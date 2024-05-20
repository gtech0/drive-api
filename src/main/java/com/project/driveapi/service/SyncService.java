package com.project.driveapi.service;

import com.project.driveapi.dto.SyncChangeDto;
import com.project.driveapi.dto.SyncGetDto;
import com.project.driveapi.entity.ClientSyncEntity;
import com.project.driveapi.entity.PathEntity;
import com.project.driveapi.exception.NotFoundException;
import com.project.driveapi.repository.ClientSyncRepository;
import com.project.driveapi.repository.PathRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import static java.nio.file.StandardWatchEventKinds.*;

@Component
public class SyncService {

    private final ClientSyncRepository syncRepository;
    private final PathRepository pathRepository;
    private final CommonService commonService;
    private final WatchService watchService = FileSystems.getDefault().newWatchService();

    public SyncService(ClientSyncRepository syncRepository,
                       PathRepository pathRepository,
                       CommonService commonService) throws IOException {
        this.syncRepository = syncRepository;
        this.pathRepository = pathRepository;
        this.commonService = commonService;
    }

    @Transactional
    public void addFolderPath(SyncChangeDto syncPath) throws IOException {
        String clientId = commonService.getFlow().getClientId();
        ClientSyncEntity syncEntity = syncRepository
                .findById(clientId)
                .orElseGet(() -> ClientSyncEntity.builder()
                        .clientId(clientId)
                        .build());

        String path = syncPath.getPath();
        if (!Files.exists(Paths.get(path))) {
            throw new IOException(path + " is not a directory");
        }

        PathEntity pathEntity = PathEntity.builder()
                .id(UUID.randomUUID())
                .path(path)
                .sync(syncEntity)
                .build();

        syncEntity.getPaths().add(pathEntity);
        syncRepository.save(syncEntity);
    }

    @Transactional
    public void removeFolderPath(SyncChangeDto syncPath) throws IOException {
        String clientId = commonService.getFlow().getClientId();
        ClientSyncEntity syncEntity = syncRepository
                .findById(clientId)
                .orElseThrow(() -> new NotFoundException("Client has no paths"));

        String path = syncPath.getPath();
        if (!Files.exists(Paths.get(path))) {
            throw new IOException(path + " is not a directory");
        }

        PathEntity pathEntity = pathRepository.findBySyncClientIdAndPath(clientId, path);
        syncEntity.getPaths().remove(pathEntity);
        syncRepository.save(syncEntity);
    }

    public SyncGetDto listPaths() throws IOException {
        String clientId = commonService.getFlow().getClientId();
        List<PathEntity> pathEntities = pathRepository.findBySyncClientId(clientId);

        List<String> paths = new ArrayList<>();
        for (PathEntity pathEntity : pathEntities) {
            paths.add(pathEntity.getPath());
        }

        return SyncGetDto.builder()
                .paths(paths)
                .build();
    }

    @Scheduled(initialDelay = 2000, fixedRate = 4000)
    protected void scheduled() {
        Thread thread = new Thread(
                () -> {
                    try {
                        watch();
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
        thread.start();
    }

    //@Scheduled(initialDelay = 2000, fixedDelay = 2000)
    private void watch() throws IOException, InterruptedException {
        String clientId = commonService.getFlow().getClientId();
        List<PathEntity> pathEntities = pathRepository.findBySyncClientId(clientId);

        List<Path> paths = new ArrayList<>();
        for (PathEntity pathEntity : pathEntities) {
            paths.add(Path.of(pathEntity.getPath()));
        }
        registerRecursive(paths);

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
}
