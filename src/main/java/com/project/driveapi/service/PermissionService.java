package com.project.driveapi.service;

import com.google.api.services.drive.model.Permission;
import com.project.driveapi.dto.PermissionDto;
import com.project.driveapi.entity.PermissionTypeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final CommonService commonService;

    public void addPermission(PermissionDto permission, String fileId) throws Exception {
        Permission newPermission = new Permission();
        newPermission.setType(permission.getType().name());
        newPermission.setRole(permission.getRole().name());
        if (permission.getType() == PermissionTypeEnum.user || permission.getType() == PermissionTypeEnum.group) {
            newPermission.setEmailAddress(permission.getEmailAddressOrDomain());
        } else if (permission.getType() == PermissionTypeEnum.domain) {
            newPermission.setDomain(permission.getEmailAddressOrDomain());
        }

        commonService.getDrive()
                .permissions()
                .create(fileId, newPermission)
                .execute();
    }

    public void revokePermission(String permissionId, String fileId) throws Exception {
        commonService.getDrive()
                .permissions()
                .delete(fileId, permissionId)
                .execute();
    }

}
