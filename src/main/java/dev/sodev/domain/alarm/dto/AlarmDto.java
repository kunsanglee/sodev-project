package dev.sodev.domain.alarm.dto;

import dev.sodev.domain.alarm.Alarm;
import dev.sodev.domain.alarm.AlarmArgs;
import dev.sodev.domain.enums.AlarmType;

import java.time.LocalDateTime;

public record AlarmDto(
        Long id,
        AlarmMemberDto member,
        AlarmType type,
        AlarmArgs args
//        LocalDateTime createdAt,
//        String createdBy,
//        LocalDateTime modifiedAt,
//        String modifiedBy
) {
    public static AlarmDto of(Alarm alarm) {
        return new AlarmDto(
                alarm.getId(),
                AlarmMemberDto.of(alarm.getMember()),
                alarm.getType(),
                alarm.getArgs()
//                alarm.getCreatedAt(),
//                alarm.getCreatedBy(),
//                alarm.getModifiedAt(),
//                alarm.getModifiedBy()
        );
    }
}
