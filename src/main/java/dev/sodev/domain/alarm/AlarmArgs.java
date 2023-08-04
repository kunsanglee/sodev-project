package dev.sodev.domain.alarm;


public record AlarmArgs(
        Long fromUserId,
        Long targetId
) {
}
