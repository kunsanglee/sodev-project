package dev.sodev.domain.follow.dto;

import lombok.Builder;

@Builder
public record FollowDto(Long memberId, String email, String nickName) {
}
