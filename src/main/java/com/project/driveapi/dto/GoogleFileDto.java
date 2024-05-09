package com.project.driveapi.dto;

import com.google.api.client.util.DateTime;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Builder
@Data
public class GoogleFileDto implements Serializable {

    private String id;

    private String name;

    private String mimeType;

    private DateTime createdTime;

    private DateTime modifiedTime;

    private boolean trashed;

    private Long size;

    private List<String> parents;

}
