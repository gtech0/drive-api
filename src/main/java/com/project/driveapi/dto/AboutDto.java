package com.project.driveapi.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class AboutDto {

    private String emailAddress;

    private String displayName;

    private String photoLink;

    private Long storageLimit;

    private Long storageUsageInDrive;

    private Long storageUsageInDriveTrash;

}
