package dev.sodev.domain.alarm.service;

import dev.sodev.domain.alarm.dto.AlarmDto;
import dev.sodev.domain.enums.AlarmType;
import dev.sodev.domain.member.Member;
import dev.sodev.domain.project.Project;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface AlarmService {

    Slice<AlarmDto> alarmList(Pageable pageable);

    void alarmsToMember(Long memberId, Long projectId, AlarmType alarmType);

    void alarmsToFollower(Long memberId, Long projectId, AlarmType alarmType);

    void alarmsToLikes(Long memberId, Long projectId, AlarmType alarmType);
}
