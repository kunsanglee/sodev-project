package dev.sodev.domain.alarm.controller;

import dev.sodev.domain.alarm.service.AlarmService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AlarmControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired AlarmService alarmService;

    @Test
    @WithMockUser
    public void 알람리스트_요청_성공() throws Exception {
        // given
        when(alarmService.alarmList(any())).thenReturn(Page.empty());
        mockMvc.perform(get("/v1/members/alarms")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        // when

        // then
    }

    @Test
    @WithAnonymousUser
    public void 알람리스트_요청시_로그인_하지_않았을_경우_실패() throws Exception {
        // given
        when(alarmService.alarmList(any())).thenReturn(Page.empty());
        mockMvc.perform(get("/v1/members/alarms")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        // when

        // then
    }
}