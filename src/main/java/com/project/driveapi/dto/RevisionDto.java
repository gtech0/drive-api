package com.project.driveapi.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class RevisionDto {

    private String id;

    private Object lastModifyingUser;

    private String mimeType;

    private LocalDateTime modifiedTime;

    private String originalFileName;

    private Long size;

}
