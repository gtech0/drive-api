package com.project.driveapi.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.project.driveapi.dto.*;
import com.project.driveapi.service.DriveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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

    @PostMapping(value = "/sign-in")
    public void googleSignIn(HttpServletResponse response) throws Exception {
        driveService.googleSignIn(response, flow);
    }

    @PostMapping(value = "/oauth")
    public String saveAuthorizationCode(HttpServletRequest request) throws Exception {
        return driveService.saveAuthorizationCode(request, flow);
    }

    @PostMapping(value = "/files/upload")
    public List<String> uploadFiles(@RequestParam(value = "files") List<MultipartFile> multipartFiles,
                                    @RequestParam(value = "targetFolderId", required = false) String targetFolderId
    ) throws Exception {
        return driveService.uploadFiles(flow, multipartFiles, targetFolderId);
    }

    @GetMapping(value = "/files/download")
    public void downloadFiles(@Parameter(example = "{ \"fileId1\": \"absolutePath1\", \"fileId2\": \"absolutePath2\" }")
                              @RequestBody Map<String, String> download) throws Exception {
        driveService.downloadFiles(flow, download);
    }

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

    @Operation(description = "Delete files completely")
    @PostMapping(value = "/files/delete")
    public void deleteFiles(@RequestBody List<String> files) throws Exception {
        driveService.deleteFiles(flow, files);
    }

    @Operation(description = "Add files to trash")
    @PostMapping(value = "/files/trash")
    public void trashFiles(@RequestBody List<String> files) throws Exception {
        driveService.trashFiles(flow, files);
    }

    @Operation(description = "Remove files from trash")
    @PostMapping(value = "/files/untrash")
    public void untrashFiles(@RequestBody List<String> files) throws Exception {
        driveService.untrashFiles(flow, files);
    }

    @Operation(description = "Delete all files from trash")
    @DeleteMapping(value = "/files/trash")
    public void emptyTrash() throws Exception {
        driveService.emptyTrash(flow);
    }

    @PostMapping(value = "/files/{fileId}/permission/add")
    public void addPermission(@RequestBody PermissionDto permission, @PathVariable String fileId) throws Exception {
        driveService.addPermission(flow, permission, fileId);
    }

    @PostMapping(value = "/files/{fileId}/permission/revoke/{permissionId}")
    public void revokePermission(@PathVariable String permissionId, @PathVariable String fileId) throws Exception {
        driveService.revokePermission(flow, permissionId, fileId);
    }
}
