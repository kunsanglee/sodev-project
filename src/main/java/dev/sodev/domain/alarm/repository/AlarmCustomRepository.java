package dev.sodev.domain.alarm.repository;

import dev.sodev.domain.alarm.AlarmArgs;
import dev.sodev.domain.enums.AlarmType;
import dev.sodev.domain.member.Member;

import java.util.List;

public interface AlarmCustomRepository {

    void bulkAlarmsSave(List<Member> members, AlarmType alarmType, AlarmArgs args);
}
