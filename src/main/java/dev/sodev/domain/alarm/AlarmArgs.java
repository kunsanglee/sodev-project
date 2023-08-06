package dev.sodev.domain.alarm;


public record AlarmArgs(
        Long fromUserId,
        String fromUserNickName,
        Long targetId,
        String targetTitle
) {
}
