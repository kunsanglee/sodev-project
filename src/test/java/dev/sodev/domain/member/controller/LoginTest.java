package dev.sodev.domain.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.sodev.domain.member.Member;
import dev.sodev.domain.enums.Auth;
import dev.sodev.domain.member.dto.request.MemberLoginRequest;
import dev.sodev.domain.member.repository.MemberRepository;
import dev.sodev.global.redis.RedisService;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;


import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.SET_COOKIE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class LoginTest {

    @Autowired MockMvc mockMvc;
    @Autowired MemberRepository memberRepository;
    @Autowired EntityManager em;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired RedisService redisService;

    ObjectMapper objectMapper = new ObjectMapper();

    private static String LOGIN_URL = "/v1/login";
    private static String EMAIL = "member@email.com";
    private static String PASSWORD = "asdf1234!";
    private static String CACHEPREFIX = "member_login::";

    private void clear() {
        em.flush();
        em.clear();
        redisService.delete(CACHEPREFIX + EMAIL);
    }

    @BeforeEach
    private void init() {
        memberRepository.save(Member.builder()
                .email(EMAIL)
                .password(passwordEncoder.encode(PASSWORD))
                .nickName("NickName1")
                .auth(Auth.MEMBER)
                .build());
        clear();

    }

    @Test
    public void 로그인_성공_토큰을_반환받고_reids_캐시가_저장된다() throws Exception {
        // given
        MemberLoginRequest request = MemberLoginRequest.builder()
                .email(EMAIL)
                .password(PASSWORD).build();

        // when
        MvcResult result = mockMvc.perform(
                        post(LOGIN_URL)
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()).andReturn();

        // then
        String accessToken = result.getResponse().getHeader("Authorization");
        String refreshToken = result.getResponse().getHeader(SET_COOKIE);
        assertThat(accessToken).startsWith("Bearer ");
        assertThat(refreshToken).startsWith("refresh-token=");
        assertThat(redisService.hasKey(CACHEPREFIX + EMAIL)).isTrue();
    }

    @Test
    public void 로그인_아이디_오류_실패_토큰과_redis_캐시가_없어야_한다() throws Exception {
        // given
        MemberLoginRequest request = MemberLoginRequest.builder()
                .email(EMAIL +1)
                .password(PASSWORD).build();

        // when
        ResultActions result = mockMvc.perform(
                post("/v1/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)));

        assertThat(result.andReturn().getResponse().getHeader("Authorization")).isNull();
        assertThat(result.andReturn().getResponse().getHeader(SET_COOKIE)).isNull();
        assertThat(result.andReturn().getResolvedException().getMessage()).isEqualTo("username is not found");
        assertThat(redisService.hasKey(CACHEPREFIX + EMAIL)).isFalse();
    }

    @Test
    public void 로그인_비밀번호_오류_실패_토큰과_redis_캐시가_없어야_한다() throws Exception {
        // given
        MemberLoginRequest request = MemberLoginRequest.builder()
                .email(EMAIL)
                .password(PASSWORD+111).build();

        // when
        ResultActions result = mockMvc.perform(
                        post("/v1/login")
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());

        // then
        assertThat(result.andReturn().getResponse().getHeader("Authorization")).isNull();
        assertThat(result.andReturn().getResponse().getHeader(SET_COOKIE)).isNull();
        assertThat(redisService.hasKey(CACHEPREFIX + EMAIL)).isFalse();
    }

}