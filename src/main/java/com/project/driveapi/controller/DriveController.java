package com.project.driveapi.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.project.driveapi.dto.FolderDto;
import com.project.driveapi.dto.GoogleFileDto;
import com.project.driveapi.dto.GoogleFileShortDto;
import com.project.driveapi.service.DriveService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/drive")
@RequiredArgsConstructor
public class DriveController {

    private final DriveService driveService;
    private GoogleAuthorizationCodeFlow flow;

    @PostConstruct
    public void init() throws IOException {
        flow = driveService.getFlow();
    }

    @GetMapping(value = "/sign-in")
    public void googleSignIn(HttpServletResponse response) throws Exception {
        driveService.googleSignIn(response, flow);
    }

    @GetMapping(value = "/oauth")
    public String saveAuthorizationCode(HttpServletRequest request) throws Exception {
        return driveService.saveAuthorizationCode(request, flow);
    }

    @PostMapping(value = "/files/upload")
    public List<String> uploadFiles(@RequestParam(value = "files") List<MultipartFile> multipartFiles,
                                    @RequestParam(value = "targetFolderId", required = false) String targetFolderId
    ) throws Exception {
        return driveService.uploadFiles(flow, multipartFiles, targetFolderId);
    }

//    @GetMapping(value = "/files/download")
//    public List<String> downloadFiles() throws Exception {
//
//    }

    @PostMapping(value = "/files/create/folder")
    public String createFolder(@RequestBody FolderDto folder) throws Exception {
        return driveService.createFolder(flow, folder);
    }

    @GetMapping(value = "/files")
    public List<GoogleFileDto> listFiles() throws Exception {
        return driveService.listFiles(flow);
    }

    @GetMapping(value = "/files/get/{fileId}")
    public GoogleFileShortDto getFile(@PathVariable String fileId) throws Exception {
        return driveService.getFile(flow, fileId);
    }

    @DeleteMapping(value = "/files/delete/{fileId}")
    public void deleteFile(@PathVariable String fileId) throws Exception {
        driveService.deleteFile(flow, fileId);
    }

    @PostMapping(value = "/files/trash/{fileId}")
    public void trashFile(@PathVariable String fileId) throws Exception {
        driveService.trashFile(flow, fileId);
    }

    @PostMapping(value = "/files/untrash/{fileId}")
    public void untrashFile(@PathVariable String fileId) throws Exception {
        driveService.untrashFile(flow, fileId);
    }

    @DeleteMapping(value = "/files/trash")
    public void emptyTrash() throws Exception {
        driveService.emptyTrash(flow);
    }
}
