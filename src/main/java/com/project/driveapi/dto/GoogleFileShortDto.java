package com.project.driveapi.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class GoogleFileShortDto {

    private String id;

    private String name;

    private String mimeType;

}
