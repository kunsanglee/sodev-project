package dev.sodev.domain.comment.service;

import dev.sodev.domain.comment.dto.request.CommentDeleteRequest;
import dev.sodev.domain.comment.dto.request.CommentRequest;
import dev.sodev.domain.comment.dto.response.CommentListResponse;
import dev.sodev.domain.comment.dto.response.CommentResponse;


public interface CommentService {

    CommentResponse createComment(Long projectId, CommentRequest request);
    CommentListResponse getAllCommentsByProjectId(Long projectId);

    CommentResponse updateComment(Long projectId, CommentRequest request);

    CommentResponse deleteComment(Long projectId, CommentDeleteRequest request);

    CommentListResponse getAllCommentsByMember();

    CommentListResponse getAllCommentsByOtherMember(Long memberId);

}
