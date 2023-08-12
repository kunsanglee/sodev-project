package dev.sodev.domain.comment.controller;

import dev.sodev.domain.comment.dto.request.CommentDeleteRequest;
import dev.sodev.domain.comment.dto.request.CommentRequest;
import dev.sodev.domain.comment.dto.response.CommentListResponse;
import dev.sodev.domain.comment.dto.response.CommentResponse;
import dev.sodev.domain.comment.service.CommentServiceImpl;
import dev.sodev.global.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Comment", description = "댓글 api")
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class CommentController {

    private final CommentServiceImpl commentService;

    @Operation(summary = "해당 프로젝트에 있는 댓글 리스트 요청")
    @GetMapping("/projects/{projectId}/comments")
    public Response<CommentListResponse> getAllCommentsByFeed(@PathVariable Long projectId) {
        CommentListResponse response = commentService.getAllCommentsByProjectId(projectId);
        return Response.success(response);
    }

    @Operation(summary = "댓글 등록", description = "댓글 내용을 입력하여 댓글을 작성합니다.")
    @PostMapping("/projects/{projectId}/comments")
    public Response<CommentResponse> createComment(@PathVariable Long projectId, @RequestBody @Valid CommentRequest request) {
        CommentResponse response = commentService.createComment(projectId, request);
        return Response.success(response);
    }

    @Operation(summary = "댓글 수정", description = "프로젝트 id와 수정하고자 하는 댓글 id와 내용을 입력하여 기존 댓글을 수정합니다.")
    @PutMapping("/projects/{projectId}/comments")
    public Response<CommentResponse> updateComment(@PathVariable Long projectId, @RequestBody @Valid CommentRequest request) {
        CommentResponse response = commentService.updateComment(projectId, request);
        return Response.success(response);
    }

    @Operation(summary = "댓글 삭제", description = "프로젝트 id와 삭제하고자 하는 댓글 id로 삭제 요청합니다.")
    @DeleteMapping("/projects/{projectId}/comments")
    public Response<CommentResponse> deleteComment(@PathVariable Long projectId, @RequestBody @Valid CommentDeleteRequest request) {
        CommentResponse response = commentService.deleteComment(projectId, request);
        return Response.success(response);
    }

    @Operation(summary = "요청자가 작성한 댓글리스트 요청", description = "요청자 본인이 작성한 댓글 리스트를 요청합니다.")
    @GetMapping("/members/comments")
    public Response<CommentListResponse> getAllCommentsByMember() {
        CommentListResponse response = commentService.getAllCommentsByMember();
        return Response.success(response);
    }

    @Operation(summary = "다른 회원의 댓글리스트 요청", description = "회원의 id로 해당 회원이 작성한 댓글 리스트를 요청합니다.")
    @GetMapping("/members/{memberId}/comments")
    public Response<CommentListResponse> getAllCommentsByOtherMember(@PathVariable Long memberId) {
        CommentListResponse response = commentService.getAllCommentsByOtherMember(memberId);
        return Response.success(response);
    }
}
