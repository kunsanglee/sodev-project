package dev.sodev.domain.follow.dto;

public record FollowResponse<T>(
        String message,
        T result
) {
}
