package com.project.driveapi.service;

import com.project.driveapi.component.CustomFileAlterationListener;
import com.project.driveapi.dto.SyncChangeDto;
import com.project.driveapi.dto.SyncGetDto;
import com.project.driveapi.entity.ClientSyncEntity;
import com.project.driveapi.entity.PathEntity;
import com.project.driveapi.exception.ControllerErrorHandler;
import com.project.driveapi.exception.NotFoundException;
import com.project.driveapi.repository.ClientSyncRepository;
import com.project.driveapi.repository.PathRepository;
import jakarta.annotation.PostConstruct;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Component
public class SyncService {

    private final ClientSyncRepository syncRepository;
    private final PathRepository pathRepository;
    private final CommonService commonService;
    private final long POLLING_INTERVAL = 1500;

    public SyncService(ClientSyncRepository syncRepository,
                       PathRepository pathRepository,
                       CommonService commonService) {
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

    @PostConstruct
    public void monitor() throws Exception {
        FileAlterationMonitor monitor = new FileAlterationMonitor(POLLING_INTERVAL);

        String clientId = commonService.getFlow().getClientId();
        List<PathEntity> pathEntities = pathRepository.findBySyncClientId(clientId);
        for (PathEntity pathEntity : pathEntities) {
            String path = pathEntity.getPath();
            File directory = new File(path);
            if (!directory.exists()) {
                throw new NotFoundException("Directory not found: " + directory);
            }

            FileAlterationObserver observer = new FileAlterationObserver(directory);
            FileAlterationListener listener = new CustomFileAlterationListener(
                    Path.of(path),
                    commonService,
                    new ControllerErrorHandler()
            );
            observer.addListener(listener);
            monitor.addObserver(observer);
        }

        monitor.start();
    }
}
