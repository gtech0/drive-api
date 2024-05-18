package com.project.driveapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionGetAnyoneDto {

    private String id;

    private String role;

    private String type;

}
