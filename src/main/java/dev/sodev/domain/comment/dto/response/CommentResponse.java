package dev.sodev.domain.comment.dto.response;

import dev.sodev.domain.comment.dto.CommentDto;
import lombok.Builder;


@Builder
public record CommentResponse(
        String message,
        CommentDto comment
) {

}
