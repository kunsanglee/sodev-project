package dev.sodev.domain.alarm.dto;

import dev.sodev.domain.alarm.Alarm;
import dev.sodev.domain.alarm.AlarmArgs;
import dev.sodev.domain.enums.AlarmType;


public record AlarmDto(
        Long id,
        AlarmMemberDto member,
        AlarmType type,
        AlarmArgs args

) {
    public static AlarmDto fromEntity(Alarm alarm) {
        return new AlarmDto(
                alarm.getId(),
                AlarmMemberDto.fromMemberEntity(alarm.getMember()),
                alarm.getType(),
                alarm.getArgs()
        );
    }
}
