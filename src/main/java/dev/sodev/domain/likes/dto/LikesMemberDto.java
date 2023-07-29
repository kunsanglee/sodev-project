package dev.sodev.domain.likes.dto;

import dev.sodev.domain.likes.Likes;
import lombok.Builder;

@Builder
public record LikesMemberDto(
        Long id,
        Long memberId,
        String nickName
) {
    public static LikesMemberDto of(Likes likes) {
        return LikesMemberDto.builder()
                .id(likes.getId())
                .memberId(likes.getMember().getId())
                .nickName(likes.getMember().getNickName())
                .build();
    }
}
