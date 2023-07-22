package dev.sodev.domain.comment.dto.response;

import dev.sodev.domain.comment.dto.CommentDto;
import lombok.Builder;

import java.util.List;

@Builder
public record CommentListResponse(
        String message,
        List<CommentDto> comments
) {
}
