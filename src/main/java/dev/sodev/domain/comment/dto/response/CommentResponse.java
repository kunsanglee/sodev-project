package dev.sodev.domain.comment.dto.response;

import dev.sodev.domain.comment.dto.CommentDto;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record CommentResponse(
        String message,
        CommentDto comment
) {

}
