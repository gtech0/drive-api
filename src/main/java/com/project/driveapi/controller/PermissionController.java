package com.project.driveapi.controller;

import com.project.driveapi.dto.PermissionDto;
import com.project.driveapi.service.DriveService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/drive")
@RequiredArgsConstructor
public class PermissionController {

    private final DriveService driveService;

    @PostMapping(value = "/files/{fileId}/permission/add")
    public void addPermission(@RequestBody PermissionDto permission, @PathVariable String fileId) throws Exception {
        driveService.addPermission(permission, fileId);
    }

    @PostMapping(value = "/files/{fileId}/permission/revoke/{permissionId}")
    public void revokePermission(@PathVariable String permissionId, @PathVariable String fileId) throws Exception {
        driveService.revokePermission(permissionId, fileId);
    }

}
