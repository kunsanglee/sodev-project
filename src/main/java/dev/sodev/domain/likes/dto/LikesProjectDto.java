package dev.sodev.domain.likes.dto;

import dev.sodev.domain.likes.Likes;
import lombok.Builder;

@Builder
public record LikesProjectDto(
        Long id,
        Long projectId,
        String projectTitle
) {
    public static LikesProjectDto of(Likes likes) {
        return LikesProjectDto.builder()
                .id(likes.getId())
                .projectId(likes.getProject().getId())
                .projectTitle(likes.getProject().getTitle())
                .build();
    }
}
