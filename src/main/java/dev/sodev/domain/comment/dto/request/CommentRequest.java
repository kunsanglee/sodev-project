package dev.sodev.domain.comment.dto.request;

import dev.sodev.domain.comment.Comment;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import org.hibernate.annotations.SQLDelete;

@Builder
public record CommentRequest(

        Long id,
        Long parentId,
        @NotBlank(message = "댓글을 작성해주세요")
        String content
) {

        public Comment of(CommentRequest request) {
                return Comment.builder()
                        .content(request.content())
                        .build();
        }

}
