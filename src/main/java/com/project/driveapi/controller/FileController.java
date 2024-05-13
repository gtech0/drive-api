package com.project.driveapi.controller;

import com.project.driveapi.dto.FolderDto;
import com.project.driveapi.dto.GoogleFileDto;
import com.project.driveapi.dto.GoogleFileShortDto;
import com.project.driveapi.service.DriveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/drive")
@RequiredArgsConstructor
public class FileController {

    private final DriveService driveService;

    @PostMapping(value = "/files/upload")
    public List<String> uploadFiles(@RequestParam(value = "files") List<MultipartFile> multipartFiles,
                                    @RequestParam(value = "targetFolderId", required = false) String targetFolderId
    ) throws Exception {
        return driveService.uploadFiles(multipartFiles, targetFolderId);
    }

    @GetMapping(value = "/files/download")
    public void downloadFiles(@Parameter(example = "{ \"fileId1\": \"absolutePath1\", \"fileId2\": \"absolutePath2\" }")
                              @RequestBody Map<String, String> download) throws Exception {
        driveService.downloadFiles(download);
    }

    @PostMapping(value = "/files/create/folder")
    public String createFolder(@RequestBody FolderDto folder) throws Exception {
        return driveService.createFolder(folder);
    }

    @GetMapping(value = "/files")
    public List<GoogleFileDto> listFiles() throws Exception {
        return driveService.listFiles();
    }

    @GetMapping(value = "/files/get/{fileId}")
    public GoogleFileShortDto getFile(@PathVariable String fileId) throws Exception {
        return driveService.getFile(fileId);
    }

    @Operation(description = "Delete files completely")
    @PostMapping(value = "/files/delete")
    public void deleteFiles(@RequestBody List<String> files) throws Exception {
        driveService.deleteFiles(files);
    }

}
