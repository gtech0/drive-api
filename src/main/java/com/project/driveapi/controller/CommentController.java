package com.project.driveapi.controller;

import com.project.driveapi.dto.CommentDto;
import com.project.driveapi.dto.CommentGetDto;
import com.project.driveapi.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/drive")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping(value = "/files/{fileId}/comments")
    public void addComment(@PathVariable String fileId, @RequestBody CommentDto comment) throws Exception {
        commentService.addComment(fileId, comment);
    }

    @PutMapping(value = "/files/{fileId}/comments/{commentId}")
    public void updateComment(@PathVariable String fileId,
                              @PathVariable String commentId,
                              @RequestBody CommentDto comment) throws Exception {
        commentService.updateComment(fileId, commentId, comment);
    }

    @GetMapping(value = "/files/{fileId}/comments/{commentId}")
    public CommentGetDto getComment(@PathVariable String fileId, @PathVariable String commentId) throws Exception {
        return commentService.getComment(fileId, commentId);
    }

    @GetMapping(value = "/files/{fileId}/comments")
    public List<CommentGetDto> getComments(@PathVariable String fileId) throws Exception {
        return commentService.getComments(fileId);
    }

    @DeleteMapping(value = "/files/{fileId}/comments/{commentId}")
    public void deleteComment(@PathVariable String fileId, @PathVariable String commentId) throws Exception {
        commentService.deleteComment(fileId, commentId);
    }

    @PostMapping(value = "/files/{fileId}/comments/{commentId}/replies")
    public void addComment(@PathVariable String fileId,
                           @PathVariable String commentId,
                           @RequestBody CommentDto comment) throws Exception {
        commentService.addReply(fileId, commentId, comment);
    }

    @PutMapping(value = "/files/{fileId}/comments/{commentId}/replies/{replyId}")
    public void updateReply(@PathVariable String fileId,
                            @PathVariable String commentId,
                            @PathVariable String replyId,
                            @RequestBody CommentDto comment) throws Exception {
        commentService.updateReply(fileId, commentId, replyId, comment);
    }

    @DeleteMapping(value = "/files/{fileId}/comments/{commentId}/replies/{replyId}")
    public void deleteReply(@PathVariable String fileId,
                            @PathVariable String commentId,
                            @PathVariable String replyId) throws Exception {
        commentService.deleteReply(fileId, commentId, replyId);
    }
}
