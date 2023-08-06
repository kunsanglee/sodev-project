package dev.sodev.domain.alarm.controller;

import dev.sodev.domain.alarm.dto.AlarmDto;
import dev.sodev.domain.alarm.service.AlarmService;
import dev.sodev.global.Response;
import dev.sodev.global.security.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/members/alarms")
public class AlarmController {

    private final AlarmService alarmService;

    @GetMapping(value = "/subscribe")
    public SseEmitter subscribe() {
        log.info("subscribe");
        String memberEmail = SecurityUtil.getMemberEmail();
        return alarmService.connectAlarm(memberEmail);
    }

    @GetMapping
    public Response<Slice<AlarmDto>> getAlarms(Pageable pageable) {
        Slice<AlarmDto> result = alarmService.alarmList(pageable);
        return Response.success(result);
    }
}
