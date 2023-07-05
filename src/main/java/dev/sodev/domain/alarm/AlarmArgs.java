package dev.sodev.domain.alarm;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class AlarmArgs {

    private Long fromUserId;
    private Long targetId;
}
