package dev.sodev.domain.alarm.service;

import dev.sodev.domain.alarm.dto.AlarmDto;
import dev.sodev.domain.enums.AlarmType;
import dev.sodev.domain.member.Member;
import dev.sodev.domain.project.Project;
import dev.sodev.global.kafka.event.AlarmEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface AlarmService {

    Slice<AlarmDto> alarmList(Pageable pageable);

    List<Member> alarmsToOne(Member member);

    List<Member> alarmsToMember(Project project);

    List<Member> alarmsToFollower(Member member);

    List<Member> alarmsToLikes(Project project);

    void sendAlarms(AlarmEvent event);

    SseEmitter connectAlarm(String memberEmail);
}
