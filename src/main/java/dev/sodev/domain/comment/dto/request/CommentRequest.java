package dev.sodev.domain.comment.dto.request;

import dev.sodev.domain.comment.Comment;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
@Schema(description = "Comment Request")
public record CommentRequest(

        Long id,
        Long parentId,
        @NotBlank(message = "댓글을 작성해주세요")
        @Schema(description = "Comment content", example = "안녕하세요 테스트 댓글입니다.")
        String content
) {

        public Comment toEntity(CommentRequest request) {
                return Comment.builder()
                        .content(request.content())
                        .build();
        }

}
