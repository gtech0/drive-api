package com.project.driveapi.controller;

import com.project.driveapi.dto.SyncChangeDto;
import com.project.driveapi.dto.SyncGetDto;
import com.project.driveapi.service.SyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/drive")
@RequiredArgsConstructor
public class SyncController {

    private final SyncService syncService;

    @PostMapping(value = "/sync")
    public void addFolderPaths(@RequestBody SyncChangeDto syncChangeDto) throws IOException {
        syncService.addFolderPath(syncChangeDto);
    }

    @DeleteMapping(value = "/sync")
    public void removeFolderPaths(@RequestBody SyncChangeDto syncChangeDto) throws IOException {
        syncService.removeFolderPath(syncChangeDto);
    }

    @GetMapping(value = "/sync")
    public SyncGetDto listPaths() throws IOException {
        return syncService.listPaths();
    }

//    @PostMapping(value = "/sync/start")
//    public void watch() throws IOException, InterruptedException {
//        syncService.watch();
//    }

}
