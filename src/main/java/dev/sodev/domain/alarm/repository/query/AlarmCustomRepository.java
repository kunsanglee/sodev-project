package dev.sodev.domain.alarm.repository.query;

import dev.sodev.domain.alarm.Alarm;
import dev.sodev.domain.alarm.AlarmArgs;
import dev.sodev.domain.enums.AlarmType;
import dev.sodev.domain.member.Member;

import java.util.List;

public interface AlarmCustomRepository {

    List<Alarm> bulkAlarmsSave(List<Member> members, AlarmType alarmType, AlarmArgs args);
}
