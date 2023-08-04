package dev.sodev.domain.alarm.dto;

import dev.sodev.domain.member.Member;

public record AlarmMemberDto(
        Long id,
        String nickName
) {
    public static AlarmMemberDto of(Member member) {
        return new AlarmMemberDto(member.getId(), member.getNickName());
    }
}
