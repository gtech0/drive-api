package com.project.driveapi.service;

import com.google.api.services.drive.model.Comment;
import com.google.api.services.drive.model.Reply;
import com.google.api.services.drive.model.User;
import com.project.driveapi.dto.CommentDto;
import com.project.driveapi.dto.CommentGetDto;
import com.project.driveapi.dto.ReplyGetDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommonService commonService;

    public void addComment(String fileId, CommentDto comment) throws Exception {
        Comment newComment = new Comment();
        newComment.setContent(comment.getContent());
        newComment.setAuthor(new User());
        commonService.getDrive()
                .comments()
                .create(fileId, newComment)
                .setFields("*")
                .execute();
    }

    public void updateComment(String fileId, String commentId, CommentDto comment) throws Exception {
        Comment newComment = new Comment();
        newComment.setContent(comment.getContent());
        commonService.getDrive()
                .comments()
                .update(fileId, commentId, newComment)
                .setFields("*")
                .execute();
    }

    public CommentGetDto getComment(String fileId, String commentId) throws Exception {
        Comment comment = commonService.getDrive()
                .comments()
                .get(fileId, commentId)
                .setFields("*")
                .execute();

        return commentBuilder(comment);
    }

    public List<CommentGetDto> getComments(String fileId) throws Exception {
        List<Comment> comments = commonService.getDrive()
                .comments()
                .list(fileId)
                .setFields("comments(id,content,author,createdTime,modifiedTime,replies)")
                .execute()
                .getComments();

        List<CommentGetDto> commentsDto = new ArrayList<>();
        for (Comment comment : comments) {
            commentsDto.add(commentBuilder(comment));
        }

        return commentsDto;
    }

    public void deleteComment(String fileId, String commentId) throws Exception {
        commonService.getDrive()
                .comments()
                .delete(fileId, commentId)
                .execute();
    }

    public void addReply(String fileId, String commentId, CommentDto comment) throws Exception {
        Reply newReply = new Reply();
        newReply.setContent(comment.getContent());
        commonService.getDrive()
                .replies()
                .create(fileId, commentId, newReply)
                .setFields("*")
                .execute();
    }

    public void updateReply(String fileId, String commentId, String replyId, CommentDto comment) throws Exception {
        Reply newReply = new Reply();
        newReply.setContent(comment.getContent());
        commonService.getDrive()
                .replies()
                .update(fileId, commentId, replyId, newReply)
                .setFields("*")
                .execute();
    }

    public void deleteReply(String fileId, String commentId, String replyId) throws Exception {
        commonService.getDrive()
                .replies()
                .delete(fileId, commentId, replyId)
                .execute();
    }

    private CommentGetDto commentBuilder(Comment comment) {
        User commentAuthor = comment.getAuthor();
        commentAuthor.setPhotoLink("https:" + commentAuthor.getPhotoLink());
        return CommentGetDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .replies(comment
                        .getReplies()
                        .stream()
                        .map(reply -> {
                            User replyAuthor = reply.getAuthor();
                            replyAuthor.setPhotoLink("https:" + replyAuthor.getPhotoLink());
                                    return ReplyGetDto.builder()
                                            .id(reply.getId())
                                            .content(reply.getContent())
                                            .author(replyAuthor)
                                            .createdTime(commonService.unixToLocalDateTime(reply.getCreatedTime().getValue()))
                                            .modifiedTime(commonService.unixToLocalDateTime(reply.getModifiedTime().getValue()))
                                            .build();
                                }
                        )
                        .toList()
                )
                .author(commentAuthor)
                .createdTime(commonService.unixToLocalDateTime(comment.getCreatedTime().getValue()))
                .modifiedTime(commonService.unixToLocalDateTime(comment.getModifiedTime().getValue()))
                .build();
    }
}
