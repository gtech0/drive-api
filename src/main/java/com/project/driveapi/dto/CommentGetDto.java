package com.project.driveapi.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class CommentGetDto {

    private String id;

    private String content;

    private List<ReplyGetDto> replies;

}
