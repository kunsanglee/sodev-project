package dev.sodev.domain.alarm.service;

import dev.sodev.domain.alarm.Alarm;
import dev.sodev.domain.alarm.dto.AlarmDto;
import dev.sodev.domain.alarm.emitter.EmitterRepository;
import dev.sodev.domain.alarm.repository.AlarmRepository;
import dev.sodev.domain.enums.ProjectRole;
import dev.sodev.domain.follow.Follow;
import dev.sodev.domain.likes.Likes;
import dev.sodev.domain.member.Member;
import dev.sodev.domain.member.MemberProject;
import dev.sodev.domain.member.repository.MemberRepository;
import dev.sodev.domain.project.Project;
import dev.sodev.global.exception.ErrorCode;
import dev.sodev.global.exception.SodevApplicationException;
import dev.sodev.global.kafka.event.AlarmEvent;
import dev.sodev.global.security.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlarmServiceImpl implements AlarmService {

    private final AlarmRepository alarmRepository;
    private final MemberRepository memberRepository;
    private final EmitterRepository emitterRepository;

    private static final String ALARM_NAME = "alarm";
    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;


    @Override
    public Slice<AlarmDto> alarmList(Pageable pageable) {
        String memberEmail = SecurityUtil.getMemberEmail();
        Member member = memberRepository.findByEmail(memberEmail).orElseThrow(() -> new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND));

        return alarmRepository.findAllByMember(member, pageable).map(AlarmDto::of);
    }

    @Override
    public List<Member> alarmsToOne(Member member) {
        return List.of(member);
    }

    @Override
    public List<Member> alarmsToMember(Project project) {
        return project.getMembers().stream()
                .filter(mp -> !mp.getProjectRole().getRole().equals(ProjectRole.Role.APPLICANT))
                .map(MemberProject::getMember)
                .distinct()
                .toList();
    }

    @Override
    public List<Member> alarmsToFollower(Member member) {
        return member.getFollowers().stream().map(Follow::getFromMember).toList();
    }

    @Override
    public List<Member> alarmsToLikes(Project project) {
        return project.getLikes().stream().map(Likes::getMember).toList();
    }

    @Override
    @Transactional
    public void sendAlarms(AlarmEvent event) {
        List<Member> members = new ArrayList<>();
        for (Long id : event.receiversId()) {
            members.add(memberRepository.findById(id).orElseThrow(() -> new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND)));
        }

        List<Alarm> alarms = alarmRepository.bulkAlarmsSave(members, event.alarmType(), event.args());

        log.info("send alarms");
        for (Alarm a : alarms) {
            emitterRepository.get(a.getMember().getId()).ifPresentOrElse(it -> {
                        try {
                            it.send(SseEmitter.event()
                                    .id(a.getId().toString())
                                    .name(ALARM_NAME)
                                    .data(a.getType()) // ex) "APPLICANT_ON_FEED"
                                    .data(a.getArgs()) // ex) {"fromUserId":2,"targetId":1}
                                    .data(a.getCreatedAt())); // ex) "2023-08-05T21:23:13.078515"
                        } catch (IOException exception) {
                            emitterRepository.delete(a.getMember().getId());
                            throw new SodevApplicationException(ErrorCode.ALARM_CONNECT_ERROR);
                        }
                    },
                    () -> log.info("No emitter founded")
            );
        }
    }

    @Override
    @Transactional
    public SseEmitter connectAlarm(String memberEmail) {
        Member member = memberRepository.findByEmail(memberEmail).orElseThrow(() -> new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND));
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        emitterRepository.save(member.getId(), emitter);
        emitter.onCompletion(() -> emitterRepository.delete(member.getId()));
        emitter.onTimeout(() -> emitterRepository.delete(member.getId()));

        try {
            log.info("send");
            emitter.send(SseEmitter.event()
                    .id("id")
                    .name(ALARM_NAME)
                    .data("connect completed"));
        } catch (IOException exception) {
            throw new SodevApplicationException(ErrorCode.ALARM_CONNECT_ERROR);
        }
        return emitter;
    }
}
