package com.project.driveapi.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ReplyGetDto {

    private String id;

    private String content;

}
