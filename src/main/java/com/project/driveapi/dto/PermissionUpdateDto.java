package com.project.driveapi.dto;

import com.project.driveapi.entity.PermissionRoleEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionUpdateDto {

    private PermissionRoleEnum role;

}
