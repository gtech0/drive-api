package com.project.driveapi.dto;

import com.google.api.services.drive.model.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
public class CommentGetDto {

    private String id;

    private String content;

    private User author;

    private LocalDateTime createdTime;

    private LocalDateTime modifiedTime;

    private List<ReplyGetDto> replies;

}
