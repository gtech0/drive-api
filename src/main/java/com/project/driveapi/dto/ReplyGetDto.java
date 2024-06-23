package com.project.driveapi.dto;

import com.google.api.services.drive.model.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class ReplyGetDto {

    private String id;

    private String content;

    private User author;

    private LocalDateTime createdTime;

    private LocalDateTime modifiedTime;

}
