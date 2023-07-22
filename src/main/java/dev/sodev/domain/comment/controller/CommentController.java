package dev.sodev.domain.comment.controller;

import dev.sodev.domain.comment.dto.request.CommentDeleteRequest;
import dev.sodev.domain.comment.dto.request.CommentRequest;
import dev.sodev.domain.comment.dto.response.CommentListResponse;
import dev.sodev.domain.comment.dto.response.CommentResponse;
import dev.sodev.domain.comment.service.CommentServiceImpl;
import dev.sodev.global.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class CommentController {

    private final CommentServiceImpl commentService;

    @GetMapping("/projects/{projectId}/comments")
    public Response<CommentListResponse> getAllCommentsByFeed(@PathVariable Long projectId) {
        CommentListResponse response = commentService.getAllCommentsByProjectId(projectId);
        return Response.success(response);
    }

    @PostMapping("/projects/{projectId}/comments")
    public Response<CommentResponse> createComment(@PathVariable Long projectId, @RequestBody @Valid CommentRequest request) {
        CommentResponse response = commentService.createComment(projectId, request);
        return Response.success(response);
    }

    @PutMapping("/projects/{projectId}/comments")
    public Response<CommentResponse> updateComment(@PathVariable Long projectId, @RequestBody @Valid CommentRequest request) {
        CommentResponse response = commentService.updateComment(projectId, request);
        return Response.success(response);
    }

    @DeleteMapping("/projects/{projectId}/comments")
    public Response<CommentResponse> deleteComment(@PathVariable Long projectId, @RequestBody @Valid CommentDeleteRequest request) {
        CommentResponse response = commentService.deleteComment(projectId, request);
        return Response.success(response);
    }

    @GetMapping("/members/comments")
    public Response<CommentListResponse> getAllCommentsByMember() {
        CommentListResponse response = commentService.getAllCommentsByMember();
        return Response.success(response);
    }

    @GetMapping("/members/{memberId}/comments")
    public Response<CommentListResponse> getAllCommentsByOtherMember(@PathVariable Long memberId) {
        CommentListResponse response = commentService.getAllCommentsByOtherMember(memberId);
        return Response.success(response);
    }
}
