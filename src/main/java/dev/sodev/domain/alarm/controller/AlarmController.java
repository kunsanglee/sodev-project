package dev.sodev.domain.alarm.controller;

import dev.sodev.domain.alarm.dto.AlarmDto;
import dev.sodev.domain.alarm.service.AlarmService;
import dev.sodev.global.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/members/alarms")
public class AlarmController {

    private final AlarmService alarmService;

    @GetMapping
    public Response<Slice<AlarmDto>> getAlarms(Pageable pageable) {
        Slice<AlarmDto> result = alarmService.alarmList(pageable);
        return Response.success(result);
    }
}
