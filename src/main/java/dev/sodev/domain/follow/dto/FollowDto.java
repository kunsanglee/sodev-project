package dev.sodev.domain.follow.dto;

import dev.sodev.domain.follow.Follow;
import lombok.Builder;

@Builder
public record FollowDto(Long memberId, String email, String nickName) {

    public static FollowDto follower(Follow follower) {
        return FollowDto.builder()
                .memberId(follower.getFromMember().getId())
                .email(follower.getFromMember().getEmail())
                .nickName(follower.getFromMember().getNickName())
                .build();
    }

    public static FollowDto following(Follow following) {
        return FollowDto.builder()
                .memberId(following.getToMember().getId())
                .email(following.getToMember().getEmail())
                .nickName(following.getToMember().getNickName())
                .build();
    }
}
