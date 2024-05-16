package com.project.driveapi.dto;

import com.project.driveapi.entity.PermissionRoleEnum;
import com.project.driveapi.entity.PermissionTypeEnum;
import lombok.Data;

@Data
public class PermissionDto {

    private PermissionRoleEnum role;

    private PermissionTypeEnum type;

    private String emailAddressOrDomain;

}