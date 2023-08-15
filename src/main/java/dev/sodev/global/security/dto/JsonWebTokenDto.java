package dev.sodev.global.security.dto;

import lombok.Builder;

@Builder
public record JsonWebTokenDto(
        String grantType,
        String accessToken,
        String refreshToken
) {
}
