package dev.sodev.domain.alarm.service;

import dev.sodev.domain.alarm.Alarm;
import dev.sodev.domain.alarm.dto.AlarmDto;
import dev.sodev.domain.alarm.emitter.EmitterRepository;
import dev.sodev.domain.alarm.repository.AlarmRepository;
import dev.sodev.domain.member.Member;
import dev.sodev.domain.member.repository.MemberRepository;
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
@Transactional
public class AlarmServiceImpl implements AlarmService {

    private static final String ALARM_NAME = "alarm";
    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;

    private final AlarmRepository alarmRepository;
    private final MemberRepository memberRepository;
    private final EmitterRepository emitterRepository;



    @Override
    public Slice<AlarmDto> alarmList(Pageable pageable) {
        String memberEmail = SecurityUtil.getMemberEmail();
        Member member = getMemberByEmail(memberEmail);

        return alarmRepository.findAllByMember(member, pageable).map(AlarmDto::fromEntity);
    }

    @Override
    public void sendAlarms(AlarmEvent event) {
        List<Member> members = addReceiver(event);
        List<Alarm> alarms = alarmRepository.bulkAlarmsSave(members, event.alarmType(), event.args());

        log.info("send alarms");
        send(alarms);
    }

    @Override
    public SseEmitter connectAlarm(String memberEmail) {
        Member member = getMemberByEmail(memberEmail);
        SseEmitter emitter = setupSseEmitter(member);

        sendInitialNotification(emitter);
        return emitter;
    }

    // 메일로 회원 조회.
    private Member getMemberByEmail(String memberEmail) {
        return memberRepository.findByEmail(memberEmail).orElseThrow(() -> new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND));
    }

    // AlarmEvent 의 대상 반환.
    private List<Member> addReceiver(AlarmEvent event) {
        List<Member> members = new ArrayList<>();
        event.receiversId().forEach(id -> memberRepository.findById(id).ifPresent(members::add));
        return members;
    }

    // 알람 전송.
    private void send(List<Alarm> alarms) {
        alarms.forEach(alarm -> emitterRepository.get(alarm.getMember().getId())
                .ifPresentOrElse(it -> {
                            try {
                                it.send(SseEmitter.event()
                                        .id(alarm.getId().toString())
                                        .name(ALARM_NAME)
                                        .data(alarm.getType()) // ex) "APPLICANT_ON_FEED"
                                        .data(alarm.getArgs()) // ex) {"fromUserId":2,"targetId":1}
                                        .data(alarm.getCreatedAt())); // ex) "2023-08-05T21:23:13.078515"
                            } catch (IOException exception) {
                                deleteEmitter(alarm.getMember());
                                throw new SodevApplicationException(ErrorCode.ALARM_CONNECT_ERROR);
                            }
                        },
                        () -> log.info("No emitter founded")
                ));
    }

    // emitter 삭제.
    private void deleteEmitter(Member member) {
        emitterRepository.delete(member.getId());
    }

    // SseEmitter 연결 설정.
    private SseEmitter setupSseEmitter(Member member) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        emitterRepository.save(member.getId(), emitter);
        emitter.onCompletion(() -> deleteEmitter(member));
        emitter.onTimeout(() -> deleteEmitter(member));
        return emitter;
    }

    // 최초 알람 전송.
    private static void sendInitialNotification(SseEmitter emitter) {
        try {
            log.info("send");
            emitter.send(SseEmitter.event()
                    .id("id")
                    .name(ALARM_NAME)
                    .data("connect completed"));
        } catch (IOException exception) {
            throw new SodevApplicationException(ErrorCode.ALARM_CONNECT_ERROR);
        }
    }
}
