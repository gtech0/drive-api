package com.project.driveapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DownloadDto {

    @Schema(example = "{ \"fileId\": \"absolutePath\" }")
    private Map<String, String> files;

}
