package com.project.driveapi.controller;

import com.project.driveapi.dto.FolderDto;
import com.project.driveapi.dto.GoogleFileDto;
import com.project.driveapi.dto.GoogleFileShortDto;
import com.project.driveapi.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/drive")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @Operation(description = "Upload files")
    @PostMapping(value = "/files/upload", consumes = "multipart/form-data")
    public void uploadFile(@RequestParam(value = "file") MultipartFile multipartFile,
                           @RequestParam(value = "targetFolderId", required = false) String targetFolderId
    ) throws Exception {
        fileService.uploadFile(multipartFile, targetFolderId);
    }

    @Operation(description = "Download selected file")
    @GetMapping(value = "/files/download/{fileId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileId) throws Exception {
        return fileService.downloadFile(fileId);
    }

    @Operation(description = "Create folder")
    @PostMapping(value = "/files/create/folder", consumes = "application/json")
    public void createFolder(@RequestBody FolderDto folder) throws Exception {
        fileService.createFolder(folder);
    }

    @Operation(description = "Update file")
    @PutMapping(value = "/files/update/{fileId}", consumes = "multipart/form-data")
    public void updateFile(@RequestParam(value = "file") MultipartFile multipartFile,
                           @PathVariable String fileId) throws IOException {
        fileService.updateFile(multipartFile, fileId);
    }

    @Operation(description = "View all files in drive")
    @GetMapping(value = "/files")
    public List<GoogleFileDto> listFiles(@RequestParam(name = "trashed", required = false) String trashed) throws Exception {
        return fileService.listFiles(trashed);
    }

    @Operation(description = "Get basic info about file")
    @GetMapping(value = "/files/get/{fileId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public GoogleFileShortDto getFile(@PathVariable String fileId) throws Exception {
        return fileService.getFile(fileId);
    }

    @Operation(description = "Delete files completely")
    @PostMapping(value = "/files/delete")
    public void deleteFiles(@RequestBody List<String> files) throws Exception {
        fileService.deleteFiles(files);
    }
}
