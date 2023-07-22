package dev.sodev.domain.comment.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CommentDeleteRequest(
        @NotNull
        Long id
) {

}
