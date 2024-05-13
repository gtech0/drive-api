package com.project.driveapi.dto;

import com.google.api.services.drive.model.Permission;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
public class GoogleFileDto implements Serializable {

    private String id;

    private String name;

    private String mimeType;

    private LocalDateTime createdTime;

    private LocalDateTime modifiedTime;

    private List<Permission> permissions;

    private boolean trashed;

    private Long size;

    private List<String> parents;

}
