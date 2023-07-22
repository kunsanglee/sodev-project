package dev.sodev.domain.comment.service;

import dev.sodev.domain.comment.Comment;
import dev.sodev.domain.comment.dto.CommentDto;
import dev.sodev.domain.comment.dto.request.CommentDeleteRequest;
import dev.sodev.domain.comment.dto.request.CommentRequest;
import dev.sodev.domain.comment.dto.response.CommentListResponse;
import dev.sodev.domain.comment.dto.response.CommentResponse;
import dev.sodev.domain.member.Member;
import dev.sodev.domain.project.Project;
import dev.sodev.global.exception.ErrorCode;
import dev.sodev.global.exception.SodevApplicationException;
import dev.sodev.global.security.utils.SecurityUtil;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CommentService {

    CommentResponse createComment(Long projectId, CommentRequest request);
    CommentListResponse getAllCommentsByProjectId(Long projectId);

    CommentResponse updateComment(Long projectId, CommentRequest request);

    CommentResponse deleteComment(Long projectId, CommentDeleteRequest request);

    CommentListResponse getAllCommentsByMember();

    CommentListResponse getAllCommentsByOtherMember(Long memberId);

}
