package com.project.driveapi.controller;

import com.project.driveapi.dto.PermissionAddDto;
import com.project.driveapi.dto.PermissionUpdateDto;
import com.project.driveapi.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/drive")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping(value = "/files/{fileId}/permissions/add")
    public void addPermission(@RequestBody PermissionAddDto permission, @PathVariable String fileId) throws Exception {
        permissionService.addPermission(permission, fileId);
    }

    @PatchMapping(value = "/files/{fileId}/permissions/update/{permissionId}")
    public void updatePermission(@RequestBody PermissionUpdateDto permission,
                                 @PathVariable String fileId,
                                 @PathVariable String permissionId) throws Exception {
        permissionService.updatePermission(permission, fileId, permissionId);
    }

    @PostMapping(value = "/files/{fileId}/permissions/revoke/{permissionId}")
    public void revokePermission(@PathVariable String permissionId, @PathVariable String fileId) throws Exception {
        permissionService.revokePermission(permissionId, fileId);
    }
}
