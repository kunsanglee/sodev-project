package dev.sodev.domain.comment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
@Schema(description = "Comment Delete Reqeust")
public record CommentDeleteRequest(
        @NotNull
        @Schema(description = "Comment id", example = "1L")
        Long id
) {

}
