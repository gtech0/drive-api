package com.project.driveapi.dto;

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

    private List<Object> permissions;

    private boolean trashed;

    private Long size;

    private List<String> parents;

}
