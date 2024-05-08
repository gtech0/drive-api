package com.project.driveapi.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.project.driveapi.dto.FolderDto;
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

    @PostMapping(value = "/upload")
    public List<String> uploadFiles(@RequestParam("files") List<MultipartFile> multipartFiles,
                                    @RequestParam(value = "targetFolderId", required = false) String targetFolderId
    ) throws Exception {
        return driveService.uploadFiles(flow, multipartFiles, targetFolderId);
    }

    @PostMapping(value = "/create/folder")
    public String createFolder(@RequestBody FolderDto folder) throws Exception {
        return driveService.createFolder(flow, folder);
    }
}
