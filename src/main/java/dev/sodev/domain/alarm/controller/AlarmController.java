package dev.sodev.domain.alarm.controller;

import dev.sodev.domain.alarm.dto.AlarmDto;
import dev.sodev.domain.alarm.service.AlarmService;
import dev.sodev.global.Response;
import dev.sodev.global.security.utils.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "Alarm", description = "알람 api")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/members/alarms")
public class AlarmController {

    private final AlarmService alarmService;

    @Operation(summary = "알람 SSE 구독")
    @GetMapping(value = "/subscribe")
    public SseEmitter subscribe() {
        log.info("subscribe");
        String memberEmail = SecurityUtil.getMemberEmail();
        return alarmService.connectAlarm(memberEmail);
    }

    @Operation(summary = "알람 리스트 요청", description = "Pageable 을 QueryParam 으로 넣어서 요청합니다.")
    @GetMapping
    public Response<Slice<AlarmDto>> getAlarms(Pageable pageable) {
        Slice<AlarmDto> result = alarmService.alarmList(pageable);
        return Response.success(result);
    }
}
