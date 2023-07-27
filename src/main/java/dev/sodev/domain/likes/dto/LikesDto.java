package dev.sodev.domain.likes.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;

@Builder
public record LikesDto(
        Long id,
        Long memberId,
        String nickName
) {

}
